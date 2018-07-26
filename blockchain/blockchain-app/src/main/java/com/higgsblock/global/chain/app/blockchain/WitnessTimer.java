package com.higgsblock.global.chain.app.blockchain;

import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
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
public class WitnessTimer implements IEventBusListener {

    public static long initTime;
    public static long currHeight;
    public final long WAIT_WITNESS_TIME = 20;

    @Autowired
    private IBlockChainService blockChainService;

    @Autowired
    private PeerManager peerManager;

    @Autowired
    private IWitnessService witnessService;

    public void initWitnessTime() {
        String address = peerManager.getSelf().getId();
        if (witnessService.isWitness(address)) {
            initTime = System.currentTimeMillis();
            currHeight = blockChainService.getMaxHeight();
        }
        LOGGER.info("init time={},currHeight={} ", initTime, currHeight);
    }

    public boolean checkGuarderPermissionWithTimer(Block block) {
        if (currHeight >= block.getHeight()) {
            return false;
        }
        long currTime = System.currentTimeMillis();
        long timeDifference = (currTime - initTime) / 1000;
        LOGGER.info("currTime={},timeDifference={} ", currTime, timeDifference);
        if (timeDifference >= WAIT_WITNESS_TIME && verifyBlockBelongGuarder(block)) {
            LOGGER.info("timeDifference > {} and block belong Guarder block return true", WAIT_WITNESS_TIME);
            return true;
        }
        return false;
    }

    public boolean checkGuarderPermissionWithoutTimer(Block block) {
        return verifyBlockBelongGuarder(block);
    }

    @Subscribe
    public void process(BlockPersistedEvent event) {
        String address = peerManager.getSelf().getId();
        if (witnessService.isWitness(address) && event.getHeight() > currHeight) {
            currHeight = event.getHeight();
            initTime = System.currentTimeMillis();
            LOGGER.info("BlockPersistedEvent modify init time={},currHeight={} ", initTime, currHeight);
        }
    }

    public boolean verifyBlockBelongGuarder(Block block) {
        return blockChainService.isGuarder(block.getMinerFirstPKSig().getAddress(), block.getPrevBlockHash());
    }
}