package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.blockchain.transaction.TransactionService;
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
 * @description:
 * @author: yezaiyong
 * @create: 2018-06-29 20:32
 **/
@Service
@Scope("prototype")
@Slf4j
public class WitnessTimer {

    private static long preHeight = 0;
    private static long currHeight = 0;
    private static long curSec;
    private ExecutorService executorService;
    private volatile boolean isRunning;
    static Block block;
    public volatile static boolean isCurrBlockConfirm = false;
    public static final long WAIT_WITNESS_TIME = 15;


    @Autowired
    private BlockService blockService;
    @Autowired
    private PeerManager peerManager;
    @Autowired
    private TransactionService transactionService;

    public boolean queryCurrHeightStartTime() throws InterruptedException {
        String address = peerManager.getSelf().getId();
        if (BlockService.WITNESS_ADDRESS_LIST.contains(address)) {
            currHeight = blockService.getMaxHeight();
            return startTimer();
        }
        return false;
    }

    public static boolean isCurrBlockConfirm(Block block) {
        try {
            WitnessTimer.block = block;
            TimeUnit.SECONDS.sleep(3);
            return isCurrBlockConfirm;
        } catch (InterruptedException e) {
            LOGGER.error("isCurrBlockConfirm error {}", e);
        }
        return false;
    }

    public static void instantiationBlock(Block block) {
        WitnessTimer.block = null;
        WitnessTimer.curSec = 0;
        preHeight = block.getHeight();
        currHeight = block.getHeight();
    }

    public boolean startTimer() throws InterruptedException {
        if (block == null) {
            preHeight = currHeight;
            WitnessTimer.curSec = 0;
            if (executorService == null) {
                this.start(ExecutorServices.newSingleThreadExecutor(getClass().getName(), 1));
            }
        }
        return true;
    }

    public final synchronized void start(ExecutorService executorService) {
        if (!isRunning) {
            this.executorService = executorService;
            isRunning = true;
            this.executorService.execute(() -> {
                while (isRunning) {
                    try {
                        ++curSec;
                        LOGGER.info("curSec = {} height={}", curSec, currHeight);
                        TimeUnit.SECONDS.sleep(1);
                        if (block == null) {
                            if (preHeight >= currHeight) {
                                LOGGER.info("block is null  pre >= curr;pre {} curr {} ", preHeight, currHeight);
                            } else {
                                preHeight = currHeight;
                                WitnessTimer.curSec = 0;
                            }
                        } else {
                            if (preHeight >= block.getHeight()) {
                                isCurrBlockConfirm = false;
                                continue;
                            }
                            if (curSec >= WAIT_WITNESS_TIME) {
                                isCurrBlockConfirm = false;
                                if (verifyBlockBelongCommonMiner(block)) {
                                    isCurrBlockConfirm = true;
                                }
                                continue;
                            }
                            isCurrBlockConfirm = false;
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error("startCountTime InterruptedException", e);
                    }
                }
            });

        }

    }

    public boolean verifyBlockBelongCommonMiner(Block block) {
        return transactionService.hasStake(block.getMinerFirstPKSig().getAddress(), SystemCurrencyEnum.CMINER);
    }
}