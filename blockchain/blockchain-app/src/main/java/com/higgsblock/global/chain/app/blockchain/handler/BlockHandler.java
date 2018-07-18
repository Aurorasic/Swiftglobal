package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.OrphanBlockCacheManager;
import com.higgsblock.global.chain.app.blockchain.consensus.sign.service.VoteService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.app.service.impl.BlockIndexService;
import com.higgsblock.global.chain.app.sync.InventoryNotify;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component("blockHandler")
@Slf4j
public class BlockHandler extends BaseEntityHandler<Block> {

    @Autowired
    private BlockProcessor blockProcessor;

    @Autowired
    private OrphanBlockCacheManager orphanBlockCacheManager;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private VoteService voteService;

    @Autowired
    private BlockIndexService blockIndexService;


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
        LOGGER.error("persisted block all info, success={}_height={}_block={}", success, height, hash);

        if (success && !data.isGenesisBlock()) {
            InventoryNotify inventoryNotify = new InventoryNotify();
            inventoryNotify.setHeight(height);
            Set<String> set = new HashSet<>(blockIndexService.getBlockIndexByHeight(height).getBlockHashs());
            inventoryNotify.setHashs(set);
            messageCenter.broadcast(new String[]{sourceId}, inventoryNotify);
        }
    }
}
