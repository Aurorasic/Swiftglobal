package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.*;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.network.socket.message.IMessage;
import com.higgsblock.global.chain.app.service.IBlockIndexService;
import com.higgsblock.global.chain.app.service.IBlockService;
import com.higgsblock.global.chain.app.sync.message.Inventory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component("blockHandler")
@Slf4j
public class BlockHandler extends BaseMessageHandler<Block> {
    @Autowired
    private IBlockChainService blockChainService;

    @Autowired
    private IBlockService blockService;
    @Autowired
    private IBlockIndexService blockIndexService;
    @Autowired
    private MessageCenter messageCenter;
    @Autowired
    private OrphanBlockCacheManager orphanBlockCacheManager;

    @Override
    protected boolean valid(SocketRequest<Block> request) {
        Block block = request.getData();
        //1. check: isGenesisBlock
        boolean isGenesisBlock = blockChainService.isGenesisBlock(block);
        if (isGenesisBlock) {
            return false;
        }

        //2. check: base info
        boolean isBasicValid = blockChainService.checkBlockBasicInfo(block);
        if (!isBasicValid) {
            LOGGER.error("error basic info block: {}", block.getSimpleInfo());
            return false;
        }

        return true;
    }

    @Override
    protected void process(SocketRequest<Block> request) {
        Block block = request.getData();
        String sourceId = request.getSourceId();
        boolean success = blockService.persistBlockAndIndex(block, sourceId);
        LOGGER.info("persisted block all info, success={},{}", success, block.getSimpleInfo());
        if (success) {
            broadcastInventory(request);
        }
    }

    private void broadcastInventory(SocketRequest<Block> request) {
        Block data = request.getData();
        long height = data.getHeight();
        String sourceId = request.getSourceId();
        Inventory inventory = new Inventory();
        inventory.setHeight(height);
        List<String> list = Optional.ofNullable(blockIndexService.getBlockIndexByHeight(height)).map(BlockIndex::getBlockHashs).orElse(null);
        if (CollectionUtils.isNotEmpty(list)) {
            Set<String> set = new HashSet<>(list);
            inventory.setHashs(set);
        }
        String[] excludeSourceIds = (sourceId == null) ? null : new String[]{sourceId};
        messageCenter.broadcast(excludeSourceIds, inventory);
        LOGGER.info("after persisted block, broadcast block: {}", inventory);
    }
}
