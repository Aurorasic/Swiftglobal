package com.higgsblock.global.chain.app.sync;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.impl.BlockService;
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
    private BlockProcessor blockProcessor;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private BlockService blockService;

    @Override
    protected void process(SocketRequest<BlockRequest> request) {
        BlockRequest data = request.getData();
        long height = data.getHeight();
        if (height <= 0L) {
            return;
        }
        String sourceId = request.getSourceId();
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
            blockProcessor.getBlocksByHeight(height).forEach(block -> messageCenter.unicast(sourceId, new BlockResponse(block)));
        }
    }
}

