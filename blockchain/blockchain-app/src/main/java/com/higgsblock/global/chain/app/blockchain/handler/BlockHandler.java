package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.OrphanBlockCacheManager;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component("blockHandler")
@Slf4j
public class BlockHandler extends BaseMessageHandler<Block> {

    @Autowired
    private BlockProcessor blockProcessor;

    @Autowired
    private OrphanBlockCacheManager orphanBlockCacheManager;

    @Override
    protected void process(SocketRequest<Block> request) {
        Block data = request.getData();
        long height = data.getHeight();
        String hash = data.getHash();
        String sourceId = request.getSourceId();

        if (height <= 1) {
            return;
        }
        if (orphanBlockCacheManager.isContains(hash)) {
            return;
        }

        boolean success = blockProcessor.persistBlockAndIndex(data, sourceId, data.getVersion());
        LOGGER.info("persisted block all info, success={},height={},block={}", success, height, hash);
    }
}
