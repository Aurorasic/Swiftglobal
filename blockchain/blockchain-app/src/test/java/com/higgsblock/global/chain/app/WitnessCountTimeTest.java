package com.higgsblock.global.chain.app;

import com.higgsblock.global.chain.app.api.service.UTXORespService;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @program: HiggsGlobal
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
    private UTXORespService utxoRespService;

    @Autowired
    private BlockService blockService;


    public boolean queryCurrHeight() throws InterruptedException {
        currHeight = currHeight;//blockService.getBestMaxHeight();
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
                                LOGGER.info("block is null  pre >= curr;pre = " + preHeight + " curr =" + currHeight);
                            } else {
                                preHeight = currHeight;
                                this.curSec = 0;
                                LOGGER.info("block is null  pre < curr;pre = " + preHeight + " curr =" + currHeight);
                            }
                        } else {
                            if (preHeight >= block.getHeight()) {
                                isCurrBlockConfirm = false;
                                LOGGER.info("block is not null  pre >= block height ;pre = " + preHeight + " block height =" + block.getHeight());
                                //this.block = null;
                            } else {
                                if (curSec >= 100) {
                                    isCurrBlockConfirm = true;
                                    //WitnessCountTime.curSec =0;
                                    //preHeight = block.getHeight();
                                } else {
                                    if (verifyBlock(block)) {
                                        //this.block = null;
                                        isCurrBlockConfirm = false;
                                    } else {
                                        isCurrBlockConfirm = true;
                                        //WitnessCountTime.curSec =0;
                                        //preHeight = block.getHeight();
                                    }
                                }
                                LOGGER.info("block is not null  pre < block height ;pre = " + preHeight + " block height =" + block.getHeight() + "是否应该接收该区块=" + isCurrBlockConfirm);
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
//        List<UTXO> list =utxoRespService.getUTXOsByAddress(block.getMinerFirstPKSig().getAddress());
//        boolean isCMINER =false;
//        if (list !=null){
//            for (UTXO utxo : list){
//                String currency=utxo.getOutput().getMoney().getCurrency();
//                if (currency.equals(SystemCurrencyEnum.CMINER)){
//                    isCMINER =true;
//                    continue;
//                }
//            }
//        }
//        return isCMINER;
    }

}