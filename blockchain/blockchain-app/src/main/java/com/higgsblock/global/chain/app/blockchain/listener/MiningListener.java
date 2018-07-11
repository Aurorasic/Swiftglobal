package com.higgsblock.global.chain.app.blockchain.listener;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.CandidateMiner;
import com.higgsblock.global.chain.app.common.SystemStatus;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.common.event.SystemStatusEvent;
import com.higgsblock.global.chain.app.consensus.NodeManager;
import com.higgsblock.global.chain.app.consensus.sign.service.SourceBlockService;
import com.higgsblock.global.chain.app.consensus.sign.service.WitnessService;
import com.higgsblock.global.chain.app.service.impl.BlockIdxDaoService;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author baizhengwen
 * @date 2018/4/2
 */
@Slf4j
@Component
public class MiningListener implements IEventBusListener {

    @Autowired
    private SourceBlockService sourceBlockService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private PeerManager peerManager;
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private WitnessService witnessService;
    @Autowired
    private CandidateMiner candidateMiner;

    @Autowired
    private BlockIdxDaoService blockIdxDaoService;

    /**
     * the block height which is produced recently
     */
    private volatile long miningHeight;
    private ExecutorService executorService = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(3), new ThreadPoolExecutor.DiscardOldestPolicy());
    private Future<?> future;
    private boolean isMining;

    @Subscribe
    public void process(BlockPersistedEvent event) {
        LOGGER.info("process event: {}", JSON.toJSONString(event));
        if (!isMining) {
            LOGGER.info("The system is not ready, cannot mining");
            return;
        }
        process(event.getBlockHash(), event.getHeight());
    }

    @Subscribe
    public void process(SystemStatusEvent event) {
        LOGGER.info("process event: {}", JSON.toJSONString(event));
        SystemStatus state = event.getSystemStatus();
        calculateDpos();
        if (SystemStatus.RUNNING == state) {
            isMining = true;
            //add by huangshengli  input pre blockhash when try to produce new block 2018-07-09
            BlockIndex lastBlockIndex = blockIdxDaoService.getLastBlockIndex();
            LOGGER.info("The system is ready, start mining,max height={}", lastBlockIndex.getHeight());
            if (lastBlockIndex.hasBestBlock()) {
                process(lastBlockIndex.getBestBlockHash(), lastBlockIndex.getHeight());
            } else {
                process(lastBlockIndex.getFirstBlockHash(), lastBlockIndex.getHeight());
            }
            witnessService.initWitnessTask(lastBlockIndex.getHeight() + 1);
        } else {
            isMining = false;
            LOGGER.info("The system state is changed to {}, stop mining", state);
        }
    }

    private void calculateDpos() {
        long maxHeight = blockService.getMaxHeight();
        if (maxHeight == 1L) {
            List<String> dposGroupBySn = nodeManager.getDposGroupBySn(2);
            if (CollectionUtils.isEmpty(dposGroupBySn)) {
                Block block = blockService.getBestBlockByHeight(1L);
                List<String> dposAddresses = nodeManager.calculateDposAddresses(block, 1L);
                nodeManager.persistDposNodes(0L, dposAddresses);
            }
        }
    }

    /**
     * produce a block with a specified height
     */
    private synchronized void process(String persistBlockHash, long maxHeight) {
        long expectHeight = maxHeight + 1;

        if (expectHeight < miningHeight) {
            LOGGER.info("block is produced, height={}", expectHeight);
            return;
        }
        if (expectHeight == miningHeight) {
            LOGGER.info("mining task is running, height={}", miningHeight);
            return;
        }

        candidateMiner.doMingTimer();
        // cancel running task
        if (null != future) {
            future.cancel(true);
            future = null;
            LOGGER.info("cancel mining task, height={}", miningHeight);
        }
        // check if my turn now
        String address = peerManager.getSelf().getId();
        boolean isMyTurn = nodeManager.canPackBlock(expectHeight, address, persistBlockHash);
        if (!isMyTurn) {
            return;
        }

        miningHeight = expectHeight;

        future = executorService.submit(() -> mining(expectHeight, persistBlockHash));
        int queueSize = ((ThreadPoolExecutor) executorService).getQueue().size();
        int poolSize = ((ThreadPoolExecutor) executorService).getPoolSize();

        LOGGER.info("try to produce block, height={},preBlockHash={},queueSize={},poolSize={}", expectHeight, persistBlockHash, queueSize, poolSize);
    }

    private void mining(long expectHeight, String blockHash) {
        while ((miningHeight == expectHeight) && !doMining(expectHeight, blockHash)) {
            try {
                TimeUnit.MILLISECONDS.sleep(1000 + RandomUtils.nextInt(10) * 500);
            } catch (Exception e) {
                LOGGER.error("mining exception,height=" + expectHeight, e);
            }
        }
    }

    private boolean doMining(long expectHeight, String blockHash) {
        try {
            LOGGER.info("begin to packageNewBlock,height={}", expectHeight);
            Block block = blockService.packageNewBlock(blockHash);
            if (block == null) {
                LOGGER.info("can not produce a new block,height={}", expectHeight);
                return false;
            }
            if (expectHeight != block.getHeight()) {
                LOGGER.error("the expect height={}, but {}", expectHeight, block.getHeight());
                return true;
            }
            sourceBlockService.sendBlockToWitness(block);
            return true;
        } catch (Exception e) {
            LOGGER.error("domining exception,height=" + expectHeight, e);
        }
        return false;
    }
}
