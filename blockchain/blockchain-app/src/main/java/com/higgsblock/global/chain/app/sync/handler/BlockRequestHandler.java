package com.higgsblock.global.chain.app.sync.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import com.higgsblock.global.chain.app.sync.message.BlockRequest;
import com.higgsblock.global.chain.app.sync.message.BlockResponse;
import com.higgsblock.global.chain.network.socket.message.IMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Component
@Slf4j
public class BlockRequestHandler extends BaseMessageHandler<BlockRequest> {

    @Autowired
    private IBlockChainService blockChainService;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private BlockService blockService;

    @Override
    protected boolean valid(IMessage<BlockRequest> message) {
        BlockRequest data = message.getData();
        return null != data && data.valid();
    }

    @Override
    protected void process(IMessage<BlockRequest> message) {
        BlockRequest data = message.getData();
        long height = data.getHeight();
        String sourceId = message.getSourceId();
        String hash = data.getHash();
        /**
         * send block to peer
         */
        if (null != hash) {
            Block block = blockService.getBlockByHash(hash);
            if (null != block) {
                messageCenter.unicast(sourceId, new BlockResponse(block));
            }
        } else {
            blockChainService.getBlocks(height).forEach(block -> messageCenter.unicast(sourceId, new BlockResponse(block)));
        }
    }
}

