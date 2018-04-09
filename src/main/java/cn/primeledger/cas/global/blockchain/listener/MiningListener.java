package cn.primeledger.cas.global.blockchain.listener;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.common.SystemStatus;
import cn.primeledger.cas.global.common.event.BlockPersistedEvent;
import cn.primeledger.cas.global.common.event.SystemStatusEvent;
import cn.primeledger.cas.global.common.listener.IEventBusListener;
import cn.primeledger.cas.global.consensus.NodeManager;
import cn.primeledger.cas.global.consensus.sign.service.CollectSignService;
import cn.primeledger.cas.global.network.PeerManager;
import cn.primeledger.cas.global.utils.ExecutorServices;
import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @date 2018/4/2
 */
@Slf4j
@Component
public class MiningListener implements IEventBusListener {

    @Autowired
    private CollectSignService collectSignService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private MessageCenter messageCenter;
    @Autowired
    private PeerManager peerManager;
    @Autowired
    private NodeManager nodeManager;

    /**
     * the block height which is produced recently
     */
    private long miningHeight;
    private ExecutorService executorService = ExecutorServices.newSingleThreadExecutor("mining", 2);
    private Future<?> future;
    private boolean isMining;

    @Subscribe
    public void process(BlockPersistedEvent event) {
        LOGGER.info("process event: {}", JSON.toJSONString(event));
        if (!isMining) {
            LOGGER.info("The system is not ready, cannot mining");
            return;
        }
        process();
    }

    @Subscribe
    public void process(SystemStatusEvent event) {
        LOGGER.info("process event: {}", JSON.toJSONString(event));
        SystemStatus state = event.getSystemStatus();
        if (SystemStatus.RUNNING == state) {
            isMining = true;
            LOGGER.info("The system is ready, start mining");
            process();
        } else {
            isMining = false;
            LOGGER.info("The system state is changed to {}, stop mining", state);
        }
    }

    /**
     * produce a block with a specified height
     */
    private synchronized void process() {
        long bestMaxHeight = blockService.getBestMaxHeight();
        long expectHeight = bestMaxHeight + 1;

        if (expectHeight < miningHeight) {
            LOGGER.info("block is produced, height={}", expectHeight);
            return;
        }
        if (expectHeight == miningHeight) {
            LOGGER.info("mining task is running, height={}", miningHeight);
            return;
        }

        // cancel running task
        if (null != future) {
            future.cancel(true);
            future = null;
            LOGGER.info("cancel mining task, height={}", miningHeight);
        }

        miningHeight = expectHeight;

        // check if my turn now
        String address = peerManager.getSelf().getId();
        boolean isMyTurn = nodeManager.canPackBlock(expectHeight, address);
        if (!isMyTurn) {
            LOGGER.info("it is not my turn");
            return;
        }

        future = executorService.submit(() -> mining(expectHeight));
        LOGGER.info("try to produce block, height={}", expectHeight);
    }

    private void mining(long expectHeight) {
        while (!doMining(expectHeight)) {
            try {
                TimeUnit.MILLISECONDS.sleep(1000 + RandomUtils.nextInt(10) * 200);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private boolean doMining(long expectHeight) {
        try {
            Block block = blockService.packageNewBlock();
            if (block == null) {
                LOGGER.error("can not produce a new block");
                return false;
            }
            if (expectHeight != block.getHeight()) {
                LOGGER.error("the expect height of block is {}, but {}", expectHeight, block.getHeight());
                return true;
            }

            block = collectSignService.sendBlockToWitness(block);
            if (block == null) {
                LOGGER.error("block is null after signed by others");
                return false;
            }
            if (!blockService.validBasic(block)) {
                LOGGER.error("block is invalid after signed by others");
                return false;
            }
            messageCenter.accept(block);
            LOGGER.info("produced block success, height={}", block.getHeight());
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
