package com.higgsblock.global.chain.app.sync.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.OrphanBlockCacheManager;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import com.higgsblock.global.chain.app.sync.message.BlockResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Component
@Slf4j
public class BlockResponseHandler extends BaseMessageHandler<BlockResponse> {

    @Autowired
    private BlockProcessor blockProcessor;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private BlockService blockService;

    @Autowired
    private OrphanBlockCacheManager orphanBlockCacheManager;

    @Override
    protected void process(SocketRequest<BlockResponse> request) {
        BlockResponse blockResponse = request.getData();
        String sourceId = request.getSourceId();
        Block block = blockResponse.getBlock();
        if (null == block) {
            return;
        }
        long height = block.getHeight();
        if (height <= 1) {
            return;
        }
        blockProcessor.persistBlockAndIndex(block, sourceId, block.getVersion());
    }
}
