package com.higgsblock.global.chain.app.sync.handler;

import com.higgsblock.global.chain.app.blockchain.*;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.IBlockIndexService;
import com.higgsblock.global.chain.app.service.IBlockService;
import com.higgsblock.global.chain.app.sync.SyncBlockService;
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

    @Autowired
    private SyncBlockService syncBlockService;

    @Override
    protected boolean valid(IMessage<BlockResponse> message) {
        BlockResponse blockResponse = message.getData();
        if (null == blockResponse) {
            return false;
        }
        List<Block> list = blockResponse.getBlocks();
        long height = blockResponse.getHeight();
        syncBlockService.invalidate(height);
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
        String sourceId = message.getSourceId();
        boolean isBroadcast = false;
        Block newBestBlock = null;

        for (Block block : message.getData().getBlocks()) {
            boolean success = true;

            //4.check: orphan block, maybe fetch pre block repeatedly several times, just do as this
            boolean isOrphanBlock = !blockChainService.isExistBlock(block.getPrevBlockHash());
            if (isOrphanBlock) {
                BlockFullInfo blockFullInfo = new BlockFullInfo(block.getVersion(), null, block);
                orphanBlockCacheManager.putAndRequestPreBlocks(blockFullInfo);
                LOGGER.warn("it is orphan block : {}", block.getSimpleInfo());
                continue;
            }

            try {
                newBestBlock = blockService.persistBlockAndIndex(block);
            } catch (Exception e) {
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
