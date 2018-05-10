package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockCacheManager;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.app.consensus.sign.service.WitnessService;
import com.higgsblock.global.chain.app.consensus.syncblock.Inventory;
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
    private BlockService blockService;

    @Autowired
    private BlockCacheManager blockCacheManager;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private WitnessService witnessService;

    @Override
    protected void process(SocketRequest<Block> request) {
        Block data = request.getData();
        long height = data.getHeight();
        String hash = data.getHash();
        if (blockCacheManager.isContains(hash)) {
            return;
        }
        short version = data.getVersion();
        String sourceId = request.getSourceId();

        boolean success = blockService.persistBlockAndIndex(data, sourceId, version);
        LOGGER.error("persisted block all info, success=_height={}_block={}", success, height, hash);
        if (success && !data.isgenesisBlock()) {
            Inventory inventory = new Inventory();
            inventory.setHeight(height);
            Set<String> set = new HashSet<>(blockService.getBlockIndexByHeight(height).getBlockHashs());
            inventory.setHashs(set);
            messageCenter.broadcast(new String[]{sourceId}, inventory);
            witnessService.initWitnessTask(blockService.getBestMaxHeight() + 1);
        }
    }
}
