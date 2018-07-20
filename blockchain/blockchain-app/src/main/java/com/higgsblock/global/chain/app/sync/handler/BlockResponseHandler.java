package com.higgsblock.global.chain.app.sync.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
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
    private BlockService blockService;

    @Override
    protected boolean check(SocketRequest<BlockResponse> request) {
        //// TODO: 2018/7/20/0020
        return true;
    }

    @Override
    protected void process(SocketRequest<BlockResponse> request) {
        BlockResponse blockResponse = request.getData();
        Block block = blockResponse.getBlock();
        if (null == block) {
            return;
        }
        String hash = block.getHash();
        long height = block.getHeight();
        if (height <= 1) {
            return;
        }
        boolean success = blockService.persistBlockAndIndex(block, block.getVersion());
        LOGGER.info("persisted block all info, success={},height={},block={}", success, height, hash);
    }
}
