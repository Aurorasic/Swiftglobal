package com.higgsblock.global.chain.app;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: yezaiyong
 * @create: 2018-07-02 15:01
 **/
@Slf4j
public class WitnessCountTimeTest {

    private static long preHeight = 0;
    private static long currHeight = 0;
    private static long curSec;
    private ExecutorService executorService;
    private volatile boolean isRunning;
    static Block block;
    public volatile static boolean isCurrBlockConfirm = false;

    @Autowired
    private BlockProcessor blockProcessor;


    public boolean queryCurrHeight() throws InterruptedException {
        currHeight = currHeight;
        return startTimer();
    }

    public static boolean isCurrBlockConfirm(Block block) throws InterruptedException {
        WitnessCountTimeTest.block = block;
        TimeUnit.SECONDS.sleep(5);
        return isCurrBlockConfirm;
    }

    public static void instantiateiBlock(Block block) {
        WitnessCountTimeTest.block = null;
        WitnessCountTimeTest.curSec = 0;
        preHeight = block.getHeight();
        currHeight = block.getHeight();
        ;
    }


    public boolean startTimer() throws InterruptedException {
        if (block == null) {
            preHeight = currHeight;
            this.curSec = 0;
            if (executorService == null) {
                this.start(ExecutorServices.newSingleThreadExecutor(getClass().getName(), 1));
            }
        }
        return true;
    }

    public final synchronized void start(ExecutorService executorService) {
        if (!isRunning) {
            this.executorService = executorService;
            this.executorService.execute(() -> {
                while (isRunning) {
                    try {
                        ++curSec;
                        LOGGER.info("-------------" + curSec);
                        TimeUnit.SECONDS.sleep(1);
                        if (block == null) {
                            if (preHeight >= currHeight) {
                            } else {
                                preHeight = currHeight;
                                this.curSec = 0;
                            }
                        } else {
                            if (preHeight >= block.getHeight()) {
                                isCurrBlockConfirm = false;
                            } else {
                                if (curSec >= 100) {
                                    isCurrBlockConfirm = true;
                                } else {
                                    if (verifyBlock(block)) {
                                        isCurrBlockConfirm = false;
                                    } else {
                                        isCurrBlockConfirm = true;
                                    }
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error("startCountTime InterruptedException", e);
                    }
                }
            });
            isRunning = true;
        }

    }

    public boolean verifyBlock(Block block) {
        return true;
    }

}