package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.api.service.UTXORespService;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionService;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @program: HiggsGlobal
 * @description:
 * @author: yezaiyong
 * @create: 2018-06-29 20:32
 **/
@Slf4j
public class WitnessCountTime {

    private static long preHeight = 0;
    private static long currHeight = 0;
    private static long curSec;
    private ExecutorService executorService;
    private volatile boolean isRunning ;
    static Block block;
    public volatile static boolean isCurrBlockConfirm =false;
    public static final long WAIT_WITNESS_TIME = 540;


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
        try{
            WitnessCountTime.block = block;
            TimeUnit.SECONDS.sleep(3);
            return isCurrBlockConfirm;
        }catch (InterruptedException e){
            LOGGER.error("isCurrBlockConfirm error {}",e);
        }
        return false;
    }

    public static void instantiationBlock(Block block) {
        WitnessCountTime.block = null;
        WitnessCountTime.curSec = 0;
        preHeight = block.getHeight();
        currHeight =block.getHeight();
    }

    public boolean startTimer() throws InterruptedException {
        //If the received block is not empty, it is proved to be in the receiving block, and empty indicates that the timer needs to start
        if(block == null ){
            preHeight = currHeight;
            WitnessCountTime.curSec = 0;
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
                        TimeUnit.SECONDS.sleep(1);
                        if(block == null){
                            if (preHeight >= currHeight){
                                LOGGER.info("block is null  pre >= curr;pre = "+preHeight+" curr =" +currHeight);
                            }else{
                                preHeight = currHeight;
                                WitnessCountTime.curSec =0;
                                LOGGER.info("block is null  pre < curr;pre = "+preHeight+" curr =" +currHeight);
                            }
                        }else{
                            if (preHeight >= block.getHeight()){
                                //need to wait for the block to come and continue timing. The current block is an invalid block
                                isCurrBlockConfirm =false;
                                LOGGER.info("block is not null  pre >= block height ;pre = "+preHeight+" block height =" +block.getHeight());
                            }else {
                                if (curSec >= WAIT_WITNESS_TIME){
                                    LOGGER.info("time of arrival  accept common miner block or candidate miner block");
                                    // can receive either a reserve or a normal reserve
                                    isCurrBlockConfirm = true;
                                    //WitnessCountTime.curSec =0;
                                    //preHeight = block.getHeight();
                                }else {
                                    //Only ordinary mining areas can be accepted
                                    if (verifyBlockBelongCommonMiner(block)){
                                        isCurrBlockConfirm =false;
                                        LOGGER.info("time of no arrival,only accept common miner block , but this candidate miner block");
                                    }else{
                                        LOGGER.info("time of no arrival,only accept common miner block , this block is common miner {} ",block.getHash());
                                        isCurrBlockConfirm =true;
                                        //WitnessCountTime.curSec =0;
                                        //preHeight = block.getHeight();
                                        //currHeight = preHeight;
                                    }
                                }
                                LOGGER.info("block is not null  pre < block height ;pre = "+preHeight+" block height =" +block.getHeight() +"is accept this block ="+isCurrBlockConfirm);
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
    public boolean verifyBlockBelongCommonMiner(Block block){
        return transactionService.hasStake(block.getMinerFirstPKSig().getAddress(),SystemCurrencyEnum.CMINER);
    }
}