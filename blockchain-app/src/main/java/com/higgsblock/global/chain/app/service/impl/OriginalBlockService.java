package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.consensus.message.OriginalBlock;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.service.IOriginalBlockService;
import com.higgsblock.global.chain.app.service.IVoteService;
import com.higgsblock.global.chain.app.service.IWitnessService;
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
public class OriginalBlockService implements IOriginalBlockService {

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private IVoteService voteService;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private IWitnessService witnessService;

    @Override
    public void sendOriginBlockToWitness(Block block) {
        LOGGER.info("send origin block to witness,height={},hash={}", block.getHeight(), block.getHash());
        OriginalBlock originalBlock = new OriginalBlock();
        originalBlock.setBlock(block);
        messageCenter.dispatchToWitnesses(originalBlock);
        if (witnessService.isWitness(keyPair.getAddress())) {
            voteService.addOriginalBlock(block);
        }
    }
}
