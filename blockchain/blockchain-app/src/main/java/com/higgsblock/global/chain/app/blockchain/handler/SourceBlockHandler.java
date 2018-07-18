package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.SourceBlock;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.app.blockchain.consensus.sign.service.VoteService;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 5/25/2018
 */
@Component("sourceBlockHandler")
@Slf4j
public class SourceBlockHandler extends BaseEntityHandler<SourceBlock> {

    @Autowired
    private VoteService voteService;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private BlockProcessor blockProcessor;

    @Override
    protected void process(SocketRequest<SourceBlock> request) {
        SourceBlock sourceBlock = request.getData();
        Block block;
        String sourceId = request.getSourceId();

        if (null == sourceBlock || null == (block = sourceBlock.getBlock())) {
            return;
        }

        long height = block.getHeight();
        if (!BlockProcessor.WITNESS_ADDRESS_LIST.contains(ECKey.pubKey2Base58Address(keyPair.getPubKey()))) {
            messageCenter.dispatchToWitnesses(sourceBlock);
            return;
        }

        if (voteService.isExistInBlockCache(height, block.getHash())) {
            return;
        }

        if (!blockProcessor.validSourceBlock(block, sourceId)) {
            LOGGER.info("the block is not valid {} {}", height, block.getHash());
            return;
        }
        LOGGER.info("Received sourceBlock {} ,{}", height, block.getHash());
        voteService.addSourceBlock(block);
        messageCenter.dispatchToWitnesses(sourceBlock);

    }


}
