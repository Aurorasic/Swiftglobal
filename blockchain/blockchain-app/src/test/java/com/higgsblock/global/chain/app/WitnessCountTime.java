package com.higgsblock.global.chain.app;

import com.higgsblock.global.chain.app.api.service.UTXORespService;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.consensus.sign.service.SourceBlockService;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @program: HiggsGlobal
 * @description:
 * @author: yezaiyong
 * @create: 2018-07-02 15:01
 **/
@Slf4j
public class WitnessCountTime {

    private static long preHeight =0;
    private static long currHeight = 0;
    private static long curSec;
    private ExecutorService executorService;
    private volatile boolean isRunning ;
    static Block block;
    public volatile static boolean isCurrBlockConfirm =false;

    @Autowired
    private UTXORespService utxoRespService;

    @Autowired
    private BlockService blockService;


    public boolean queryCurrHeight() throws InterruptedException {
        currHeight =currHeight;//blockService.getBestMaxHeight();
        return startTimer();
    }

    public static boolean isCurrBlockConfirm(Block block) throws InterruptedException {
        WitnessCountTime.block = block;
        TimeUnit.SECONDS.sleep(5);
        return isCurrBlockConfirm;
    }

    public static void instantiateiBlock(Block block) {
        WitnessCountTime.block = null;
        WitnessCountTime.curSec = 0;
        preHeight = block.getHeight();
        currHeight = block.getHeight();;
    }


    public boolean startTimer() throws InterruptedException {
        //如果收到区块不为空，证明是在收区块，为空表明需要开始计时
        if(block == null ){
            preHeight = currHeight;
            this.curSec = 0;
            if (executorService == null){
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
                        LOGGER.info("-------------"+curSec);
                        TimeUnit.SECONDS.sleep(1);
                        if(block == null){
                            if (preHeight >= currHeight){
                                LOGGER.info("block is null  pre >= curr;pre = "+preHeight+" curr =" +currHeight);
                            }else{
                                preHeight = currHeight;
                                this.curSec =0;
                                LOGGER.info("block is null  pre < curr;pre = "+preHeight+" curr =" +currHeight);
                            }
                        }else{
                            if (preHeight >= block.getHeight()){
                                //需要等待区块来，继续计时,当前区块为无效区块
                                isCurrBlockConfirm =false;
                                LOGGER.info("block is not null  pre >= block height ;pre = "+preHeight+" block height =" +block.getHeight());
                                //this.block = null;
                            }else {
                                if (curSec >= 100){
                                    LOGGER.info("达到时间了.可以接收候补矿工区块,或者普通矿工区块");
                                    //可以接收候补矿工区块也可也接收普通区块
                                    isCurrBlockConfirm = true;
                                    //WitnessCountTime.curSec =0;
                                    //preHeight = block.getHeight();
                                }else {
                                    //只能接收普通矿工区块
                                    if (verifyBlock(block)){
                                        //this.block = null;
                                        isCurrBlockConfirm =false;
                                        LOGGER.info("没达到时间,只能接收普通矿工区块,但是该区块是候补矿工区块");
                                    }else{
                                        LOGGER.info("没达到时间,只能接收普通矿工区块,该区块是普通矿工区块");
                                        isCurrBlockConfirm =true;
                                        //WitnessCountTime.curSec =0;
                                        //preHeight = block.getHeight();
                                    }
                                }
                                LOGGER.info("block is not null  pre < block height ;pre = "+preHeight+" block height =" +block.getHeight() +"是否应该接收该区块="+isCurrBlockConfirm);
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
    public boolean verifyBlock(Block block){

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


    public static void main(String[] args) throws InterruptedException {

        //开机启动线程
        WitnessCountTime witnessCountTime = new WitnessCountTime();
        WitnessCountTime.currHeight = 100;
        witnessCountTime.queryCurrHeight();

        TimeUnit.SECONDS.sleep(100);


        //收到101区块
        Block block1 =new Block();
        block1.setHeight(101);
        boolean is =WitnessCountTime.isCurrBlockConfirm(block1);
        System.out.println(is);
//
//        //见证完了 实例化了区块
        if (is){
           WitnessCountTime.instantiateiBlock(block1);
       }
//
//
//        TimeUnit.SECONDS.sleep(50);
//        //收到102区块
//        Block block2 =new Block();
//        block2.setHeight(102);
//        WitnessCountTime.isCurrBlockConfirm(block2);
//        //见证完了 实例化了区块
//        if (is) {
//            WitnessCountTime.instantiateiBlock(block2);
//        }
//
//        TimeUnit.SECONDS.sleep(50);
//
//        //收到103区块
//        Block block3 =new Block();
//        block3.setHeight(103);
//        WitnessCountTime.isCurrBlockConfirm(block3);
//
//        //见证完了 实例化了区块
//        if(is){
//            WitnessCountTime.instantiateiBlock(block3);
//        }
//
//        Block block4 =new Block();
//        block4.setHeight(103);
//        WitnessCountTime.isCurrBlockConfirm(block4);
    }
}