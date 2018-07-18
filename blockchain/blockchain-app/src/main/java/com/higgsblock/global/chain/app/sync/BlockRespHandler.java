package com.higgsblock.global.chain.app.sync;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.OrphanBlockCacheManager;
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
@Component("blockReqHandler")
@Slf4j
public class BlockRespHandler extends BaseEntityHandler<BlockResp> {

    @Autowired
    private BlockProcessor blockProcessor;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private BlockPersistService blockPersistService;

    @Autowired
    private OrphanBlockCacheManager orphanBlockCacheManager;


    @Override
    protected void process(SocketRequest<BlockResp> request) {
        BlockResp blockResp = request.getData();
        String sourceId = request.getSourceId();
        Block block = blockResp.getBlock();
        if (null == block) {
            return;
        }
        long height = block.getHeight();
        String hash = block.getHash();

        if (height <= 1) {
            return;
        }
        if (orphanBlockCacheManager.isContains(hash)) {
            return;
        }

        blockProcessor.persistBlockAndIndex(block, sourceId, block.getVersion());

    }
}
