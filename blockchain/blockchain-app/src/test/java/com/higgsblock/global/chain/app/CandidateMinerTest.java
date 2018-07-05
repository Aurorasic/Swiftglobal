package com.higgsblock.global.chain.app;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @program: HiggsGlobal
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
        //是否是候补矿工
        if (isCMINER) {
            currHeight = this.currHeight;
            startTimer();
        }
    }

    public void doMingTimer() {
        if (isCMINER) {
            currHeight = currHeight;//blockService.getBestMaxHeight();
            blockStatus = false;
        }
    }

    public void instantiationBlock() {
        if (isCMINER) {
            blockStatus = true;
            currHeight = currHeight;//blockService.getBestMaxHeight();

        }
    }

    public void startTimer() throws InterruptedException {
        if (executorService == null) {
            this.start(ExecutorServices.newSingleThreadExecutor(getClass().getName(), 1));
        }
    }

    public static void doMing() {
        System.out.println("打了一个区块了");
        Block block = new Block();
        block.setHeight(currHeight + 1);
        System.out.println("打出区块的高度" + block.getHeight());
        CandidateMinerTest.block = block;
    }

    public static void sendBlock() {

        System.out.println("再发送区块给见证这");

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
                                //判断是否已经打出区块，如果已经打出，再发送给见证
                                if (block != null) {
                                    sendBlock();
                                    TimeUnit.SECONDS.sleep(10);
                                } else {
                                    doMing();
                                    //TimeUnit.SECONDS.sleep(10);
                                }
                            }
                        }
                        LOGGER.info("preHeight =" + preHeight + "currHeight" + currHeight);
                        LOGGER.info("=====blockStatus = " + blockStatus);
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

    public static void main(String[] args) throws InterruptedException {
        CandidateMinerTest s = new CandidateMinerTest();
        //矿机启动
        s.currHeight = 100;
        s.queryCurrHeight();

        TimeUnit.SECONDS.sleep(40);

//
//        Timer2 s1 = new Timer2();
//        s1.doMingTimer();


        CandidateMinerTest s2 = new CandidateMinerTest();
        currHeight = 101;
        s2.instantiationBlock();
//
//        Timer2.blockStatus =true;
//        Timer2.currHeight = 101;
//
//        TimeUnit.SECONDS.sleep(40);
//
//        Timer2.blockStatus = true;
//        Timer2.currHeight = 102;

    }
}