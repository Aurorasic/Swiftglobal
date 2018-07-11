package com.higgsblock.global.chain.app;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: yezaiyong
 * @create: 2018-07-02 12:53
 **/
@Slf4j
public class CandidateMinerTest {
    private static long preHeight = 0;
    private static long currHeight = 0;
    private long curSec;
    private ExecutorService executorService;
    private volatile boolean isRunning;
    static Block block;
    public volatile static boolean blockStatus = false;
    public volatile boolean isCMINER = true;

    public void queryCurrHeight() throws InterruptedException {
        if (isCMINER) {
            currHeight = this.currHeight;
            startTimer();
        }
    }

    public void doMingTimer() {
        if (isCMINER) {
            currHeight = currHeight;
            blockStatus = false;
        }
    }

    public void instantiationBlock() {
        if (isCMINER) {
            blockStatus = true;
            currHeight = currHeight;

        }
    }

    public void startTimer() throws InterruptedException {
        if (executorService == null) {
            this.start(ExecutorServices.newSingleThreadExecutor(getClass().getName(), 1));
        }
    }

    public static void doMing() {
        Block block = new Block();
        block.setHeight(currHeight + 1);
        CandidateMinerTest.block = block;
    }

    public static void sendBlock() {

        currHeight = block.getHeight();
    }

    public final synchronized void start(ExecutorService executorService) {
        if (!isRunning) {
            this.executorService = executorService;
            this.executorService.execute(() -> {
                while (isRunning) {
                    try {
                        ++curSec;
                        LOGGER.info("curSec =" + curSec);
                        TimeUnit.SECONDS.sleep(1);
                        if (preHeight >= currHeight) {
                            if (curSec > 50) {
                                if (block != null) {
                                    sendBlock();
                                    TimeUnit.SECONDS.sleep(10);
                                } else {
                                    doMing();
                                }
                            }
                        }
                        if (preHeight < currHeight && blockStatus) {
                            this.preHeight = currHeight;
                            this.curSec = 0;
                            this.block = null;
                            blockStatus = false;
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