package com.higgsblock.global.chain.app.blockchain;

import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionService;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: yezaiyong
 * @create: 2018-07-16 10:12
 **/
@Component
@Slf4j
public class WitnessTimeProcess implements IEventBusListener {

    public static long initTime;
    public static long currHeight;
    public final long WAIT_WITNESS_TIME = 20;

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private BlockProcessor blockProcessor;
    @Autowired
    private PeerManager peerManager;

    public void initWitnessTime() {
        String address = peerManager.getSelf().getId();
        if (BlockProcessor.WITNESS_ADDRESS_LIST.contains(address)) {
            initTime = System.currentTimeMillis();
            currHeight = blockProcessor.getMaxHeight();
        }
        LOGGER.info("init time={},currHeight={} ", initTime, currHeight);
    }

    public boolean acceptBlock(Block block) {
        if (currHeight >= block.getHeight()) {
            return false;
        }
        long currTime = System.currentTimeMillis();
        long timeDifference = (currTime - initTime) / 1000;
        LOGGER.info("currTime={},timeDifference={} ", currTime, timeDifference);
        if (timeDifference >= WAIT_WITNESS_TIME && verifyBlockBelongCandidateMiner(block)) {
            LOGGER.info("timeDifference > {} and block belong candidate block return true", WAIT_WITNESS_TIME);
            return true;
        }
        return false;
    }


    @Subscribe
    public void process(BlockPersistedEvent event) {
        String address = peerManager.getSelf().getId();
        if (BlockProcessor.WITNESS_ADDRESS_LIST.contains(address) && event.getHeight() > currHeight) {
            currHeight = event.getHeight();
            initTime = System.currentTimeMillis();
            LOGGER.info("BlockPersistedEvent modify init time={},currHeight={} ", initTime, currHeight);
        }
    }

    public boolean verifyBlockBelongCandidateMiner(Block block) {
        return transactionService.hasStake(block.getMinerFirstPKSig().getAddress(), SystemCurrencyEnum.CMINER);
    }
}