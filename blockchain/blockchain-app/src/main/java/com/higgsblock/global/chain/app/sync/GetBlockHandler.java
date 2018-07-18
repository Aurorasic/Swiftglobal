package com.higgsblock.global.chain.app.sync;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.app.service.impl.BlockPersistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Component("getBlockHandler")
@Slf4j
public class GetBlockHandler extends BaseEntityHandler<GetBlock> {

    @Autowired
    private BlockService blockService;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private BlockPersistService blockPersistService;

    @Override
    protected void process(SocketRequest<GetBlock> request) {
        GetBlock data = request.getData();
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
            Block block = blockPersistService.getBlockByHash(hash);
            if (null != block) {
                messageCenter.unicast(sourceId, block);
            }
        } else {
            blockService.getBlocksByHeight(height).forEach(block -> messageCenter.unicast(sourceId, block));
        }
    }
}

