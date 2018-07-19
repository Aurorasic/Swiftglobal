package com.higgsblock.global.chain.app.blockchain.consensus.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.consensus.message.OriginalBlock;
import com.higgsblock.global.chain.app.blockchain.consensus.sign.service.VoteProcessor;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yangyi
 * @deta 2018/7/19
 * @description
 */
@Slf4j
@Component
public class OriginalBlockHandler extends BaseMessageHandler<OriginalBlock> {

    @Autowired
    private VoteProcessor voteProcessor;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private BlockProcessor blockProcessor;

    @Override
    protected void process(SocketRequest<OriginalBlock> request) {

        OriginalBlock originalBlock = request.getData();
        Block block;
        String sourceId = request.getSourceId();

        if (null == originalBlock || null == (block = originalBlock.getBlock())) {
            return;
        }

        long height = block.getHeight();
        LOGGER.info("Received OriginalBlock {} ,{}", height, block.getHash());
        if (!BlockProcessor.WITNESS_ADDRESS_LIST.contains(ECKey.pubKey2Base58Address(keyPair.getPubKey()))) {
            messageCenter.dispatchToWitnesses(originalBlock);
            return;
        }

        if (voteProcessor.isExistInBlockCache(height, block.getHash())) {
            return;
        }

        if (!blockProcessor.validOriginalBlock(block, sourceId)) {
            LOGGER.info("the block is not valid {} {}", height, block.getHash());
            return;
        }
        LOGGER.info("add OriginalBlock {} ,{}", height, block.getHash());
        voteProcessor.addOriginalBlock(block);
        messageCenter.dispatchToWitnesses(originalBlock);
    }
}
