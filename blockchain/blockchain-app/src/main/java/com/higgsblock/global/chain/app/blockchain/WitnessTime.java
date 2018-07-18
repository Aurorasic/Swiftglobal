package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.blockchain.transaction.TransactionService;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
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
public class WitnessTime {

    public static long initTime;
    public static long currHeight;
    public final long WAIT_WITNESS_TIME = 20;

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private PeerManager peerManager;

    public void initWitnessTime() {
        String address = peerManager.getSelf().getId();
        if (BlockService.WITNESS_ADDRESS_LIST.contains(address)) {
            initTime = System.currentTimeMillis();
            currHeight = blockService.getMaxHeight();
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

    public void updateMaxHeightAndInitTime(Block block) {
        String address = peerManager.getSelf().getId();
        if (BlockService.WITNESS_ADDRESS_LIST.contains(address) && block.getHeight() > currHeight) {
            currHeight = block.getHeight();
            initTime = System.currentTimeMillis();
            LOGGER.info("updateMaxHeightAndInitTime modify init time={},currHeight={} ", initTime, currHeight);
        }
    }

    public boolean verifyBlockBelongCandidateMiner(Block block) {
        return transactionService.hasStake(block.getMinerFirstPKSig().getAddress(), SystemCurrencyEnum.CMINER);
    }
}