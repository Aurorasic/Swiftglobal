package com.higgsblock.global.chain.app.blockchain.consensus.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.consensus.message.OriginBlock;
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
public class OriginBlockHandler extends BaseMessageHandler<OriginBlock> {

    @Autowired
    private VoteProcessor voteProcessor;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private BlockProcessor blockProcessor;

    @Override
    protected void process(SocketRequest<OriginBlock> request) {

        OriginBlock originBlock = request.getData();
        Block block;
        String sourceId = request.getSourceId();

        if (null == originBlock || null == (block = originBlock.getBlock())) {
            return;
        }

        long height = block.getHeight();
        if (!BlockProcessor.WITNESS_ADDRESS_LIST.contains(ECKey.pubKey2Base58Address(keyPair.getPubKey()))) {
            messageCenter.dispatchToWitnesses(originBlock);
            return;
        }

        if (voteProcessor.isExistInBlockCache(height, block.getHash())) {
            return;
        }

        if (!blockProcessor.validOriginalBlock(block, sourceId)) {
            LOGGER.info("the block is not valid {} {}", height, block.getHash());
            return;
        }
        LOGGER.info("Received votingBlockResponse {} ,{}", height, block.getHash());
        voteProcessor.addOriginalBlock(block);
        messageCenter.dispatchToWitnesses(originBlock);
    }
}
