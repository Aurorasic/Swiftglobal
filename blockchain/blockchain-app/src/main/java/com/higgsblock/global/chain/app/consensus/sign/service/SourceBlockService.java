package com.higgsblock.global.chain.app.consensus.sign.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.SourceBlock;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author yangyi
 * @date 2018/3/6
 */
@Component
@Slf4j
public class SourceBlockService {

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private WitnessService witnessService;

    @Autowired
    private KeyPair keyPair;

    /**
     * Creator sends the signed block to other witnesses for resigning.
     */
    public void sendBlockToWitness(Block block) {
        LOGGER.info("begin to send block to witness");
        SourceBlock sourceBlock = new SourceBlock(block);
        messageCenter.dispatchToWitnesses(sourceBlock);
        if (BlockService.WITNESS_ADDRESS_LIST.contains(ECKey.pubKey2Base58Address(keyPair.getPubKey()))) {
            witnessService.addCandidateBlockFromMiner(sourceBlock.getBlock());
        }
    }

}
