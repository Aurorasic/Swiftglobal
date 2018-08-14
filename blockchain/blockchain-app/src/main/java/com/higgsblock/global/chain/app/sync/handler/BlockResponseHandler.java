package com.higgsblock.global.chain.app.sync.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.OrphanBlockCacheManager;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.IBlockIndexService;
import com.higgsblock.global.chain.app.service.IBlockService;
import com.higgsblock.global.chain.app.sync.message.BlockResponse;
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
@Slf4j
@Component
public class BlockResponseHandler extends BaseMessageHandler<BlockResponse> {
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
    protected boolean valid(IMessage<BlockResponse> message) {
        BlockResponse blockResponse = message.getData();
        if (null == blockResponse) {
            return false;
        }
        List<Block> list = blockResponse.getBlocks();
        long height = blockResponse.getHeight();
        if (CollectionUtils.isEmpty(list)) {
            return false;
        }
        return list.stream().allMatch(block -> {
            if (height != block.getHeight()) {
                return false;
            }
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
        });
    }

    @Override
    protected void process(IMessage<BlockResponse> message) {
        long height = message.getData().getHeight();
        String sourceId = message.getSourceId();
        boolean isBroadcast = false;
        Block newBestBlock = null;

        for (Block block : message.getData().getBlocks()) {
            boolean success = true;
            try {
                newBestBlock = blockService.persistBlockAndIndex(block);
            } catch (Exception e) {
                LOGGER.info(String.format("BlockResponseHandler save block failed %s", block.getSimpleInfo()), e);
                success = false;
            }
            isBroadcast = isBroadcast || success;
            LOGGER.info("persisted block all info, success={},{}", success, block.getSimpleInfo());
            if (success) {
                blockService.doSyncWorksAfterPersistBlock(newBestBlock, block);
            }
        }
        if (isBroadcast) {
            broadcastInventory(message.getData().getHeight(), sourceId);
        }
    }

    private void broadcastInventory(long height, String sourceId) {
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
