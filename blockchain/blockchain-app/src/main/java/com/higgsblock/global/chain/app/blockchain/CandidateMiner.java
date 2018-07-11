package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.blockchain.transaction.TransactionService;
import com.higgsblock.global.chain.app.consensus.sign.service.SourceBlockService;
import com.higgsblock.global.chain.app.service.impl.BlockIdxDaoService;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @program: HiggsGlobal
 * @description:
 * @author: yezaiyong
 * @create: 2018-06-29 14:31
 **/
@Service
@Scope("prototype")
@Slf4j
public class CandidateMiner {

    private static long preHeight = 0;
    private static long currHeight = 0;
    private long curSec;
    private ExecutorService executorService;
    private volatile boolean isRunning;
    public static volatile boolean isCMINER = false;
    public static final long WAIT_MINER_TIME = 180;

    @Autowired
    private BlockService blockService;
    @Autowired
    private SourceBlockService sourceBlockService;
    @Autowired
    private PeerManager peerManager;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private BlockIdxDaoService blockIdxDaoService;

    public void queryCurrHeightStartTime() throws InterruptedException {
        String address = peerManager.getSelf().getId();

        isCMINER = transactionService.hasStake(address, SystemCurrencyEnum.CMINER);
        if (isCMINER) {
            currHeight = blockService.getMaxHeight();
            startTimer();
        }
    }

    public synchronized void doMingTimer() {
        String address = peerManager.getSelf().getId();
        isCMINER = transactionService.hasStake(address, SystemCurrencyEnum.CMINER);
        if (isCMINER) {
            currHeight = blockService.getMaxHeight();
        }
    }

    public void instantiationBlock() {
        String address = peerManager.getSelf().getId();
        isCMINER = transactionService.hasStake(address, SystemCurrencyEnum.CMINER);
        if (isCMINER) {
            currHeight = blockService.getMaxHeight();
        }
    }

    public void startTimer() throws InterruptedException {
        if (executorService == null) {
            this.start(ExecutorServices.newSingleThreadExecutor(getClass().getName(), 1));
        }
    }

    public boolean doMing() {
        long bestMaxHeight = currHeight;
        long expectHeight = bestMaxHeight + 1;
        try {
            BlockIndex maxBlockIndex = blockIdxDaoService.getBlockIndexByHeight(currHeight);
            if (maxBlockIndex == null) {
                throw new IllegalArgumentException("the blockIndex not found ,current height:" + currHeight);
            }
            for (String blockHash : maxBlockIndex.getBlockHashs()) {
                LOGGER.info("begin to packageNewBlock,height={},preBlcokHash={}", expectHeight, blockHash);
                Block block = blockService.packageNewBlock(blockHash);
                if (block == null) {
                    LOGGER.warn("can not produce a new block,height={},preBlcokHash={}", expectHeight, blockHash);
                    continue;
                }
                if (expectHeight != block.getHeight()) {
                    LOGGER.error("the expect height={}, but {}", expectHeight, block.getHeight());
                    return true;
                }
                sourceBlockService.sendBlockToWitness(block);
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("domining exception,height=" + expectHeight, e);
        }
        LOGGER.warn("can not produce a new block,height={}", expectHeight);
        return false;
    }

    public final synchronized void start(ExecutorService executorService) {
        if (!isRunning) {
            this.executorService = executorService;
            this.executorService.execute(() -> {
                while (isRunning) {
                    try {
                        ++curSec;
                        LOGGER.info("curSec = " + curSec);
                        TimeUnit.SECONDS.sleep(1);
                        if (preHeight >= currHeight) {
                            if (curSec > WAIT_MINER_TIME) {
                                doMing();
                                TimeUnit.SECONDS.sleep(50);
                            }
                        }
                        if (preHeight < currHeight) {
                            preHeight = currHeight;
                            this.curSec = 0;
                        }

                    } catch (InterruptedException e) {
                        LOGGER.error("startCountTime InterruptedException", e);
                    }
                }
            });
            isRunning = true;
        }
    }
}