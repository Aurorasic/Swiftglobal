package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.OrphanBlockCacheManager;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.IBlockIndexService;
import com.higgsblock.global.chain.app.service.IBlockService;
import com.higgsblock.global.chain.app.sync.message.Inventory;
import com.higgsblock.global.chain.network.socket.message.IMessage;
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
    protected boolean valid(IMessage<Block> message) {
        Block block = message.getData();
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
    protected void process(IMessage<Block> message) {
        Block block = message.getData();
        Block newBestBlock = null;
        boolean success = true;
        try {
            newBestBlock = blockService.persistBlockAndIndex(block);
        } catch (Exception e) {
            success = false;
        }

        LOGGER.info("persisted block all info, success={},{}", success, block.getSimpleInfo());
        if (success) {
            blockService.doSomeJobAfterPersistBlock(newBestBlock, block);
            broadcastInventory(message);
        }
    }

    private void broadcastInventory(IMessage<Block> message) {
        Block data = message.getData();
        long height = data.getHeight();
        String sourceId = message.getSourceId();
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
