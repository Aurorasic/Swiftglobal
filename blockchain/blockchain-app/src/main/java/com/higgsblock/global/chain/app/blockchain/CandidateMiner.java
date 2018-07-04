package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.blockchain.transaction.TransactionService;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.consensus.sign.service.SourceBlockService;
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
public class CandidateMiner{

    private static long preHeight =0;
    private static long currHeight = 0;
    private long curSec;
    private ExecutorService executorService;
    private volatile boolean isRunning ;
    Block block;
    public volatile static boolean blockStatus = false;
    public static volatile boolean isCMINER =false;
    public static final long WAIT_MINER_TIME = 600;

    @Autowired
    private static BlockService blockService;
    @Autowired
    private SourceBlockService sourceBlockService;
    @Autowired
    private PeerManager peerManager;
    @Autowired
    private TransactionService transactionService;

    public void queryCurrHeightStartTime() throws InterruptedException {
        String address = peerManager.getSelf().getId();

        isCMINER = transactionService.hasStake(address,SystemCurrencyEnum.CMINER);
        if (isCMINER){
            currHeight =blockService.getMaxHeight();
            startTimer();
        }
    }

    public static synchronized void doMingTimer(){
        if(isCMINER){
            currHeight =blockService.getMaxHeight();
            blockStatus = false;
        }
    }

    public static void instantiationBlock(){
        LOGGER.info("isCMINER =" +CandidateMiner.isCMINER);
        if(isCMINER) {
            CandidateMiner.blockStatus = true;
            currHeight = blockService.getMaxHeight();
            LOGGER.info("currHeight =" +currHeight +"blockStatus ="+CandidateMiner.blockStatus);
        }
    }

    public void startTimer() throws InterruptedException {
        if (executorService == null){
            this.start(ExecutorServices.newSingleThreadExecutor(getClass().getName(), 1));
        }
    }

    public boolean doMing(){
        long bestMaxHeight = currHeight;
        long expectHeight = bestMaxHeight + 1;
        try {
            LOGGER.info("begin to packageNewBlock,height={}",expectHeight);
            block = blockService.packageNewBlock();
            if (block == null) {
                LOGGER.info("can not produce a new block,height={}",expectHeight);
                return false;
            }
            if (expectHeight != block.getHeight()) {
                LOGGER.error("the expect height={}, but {}", expectHeight, block.getHeight());
                return true;
            }
            sourceBlockService.sendBlockToWitness(block);
            return true;
        } catch (Exception e) {
            LOGGER.error("domining exception,height="+expectHeight, e);
        }
        return false;
    }

    public void sendBlock(){
        if (null != block){
            sourceBlockService.sendBlockToWitness(block);
        }
    }

    public final synchronized void start(ExecutorService executorService) {
        if (!isRunning){
            this.executorService = executorService;
            this.executorService.execute(() -> {
                while(isRunning){
                    try {
                        ++curSec;
                        LOGGER.info("curSec = "+curSec);
                        TimeUnit.SECONDS.sleep(1);
                        if (preHeight >= currHeight){
                            if (curSec > WAIT_MINER_TIME){
                                //Determine whether the block has been hit, and if so, send it to the witness
                                if (block != null){
                                    sendBlock();
                                    TimeUnit.SECONDS.sleep(50);
                                }else{
                                    doMing();
                                }
                            }
                        }
                        LOGGER.info("preHeight ="+preHeight+"currHeight"+currHeight);
                        if (preHeight < currHeight && blockStatus){
                            CandidateMiner.preHeight = currHeight;
                            this.curSec =0;
                            this.block = null;
                            blockStatus =false;
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