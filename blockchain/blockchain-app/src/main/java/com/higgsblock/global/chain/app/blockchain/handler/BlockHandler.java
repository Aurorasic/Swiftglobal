package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.*;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.impl.BlockIndexService;
import com.higgsblock.global.chain.app.service.impl.BlockService;
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
    private BlockService blockService;
    @Autowired
    private BlockIndexService blockIndexService;
    @Autowired
    private MessageCenter messageCenter;
    @Autowired
    private OrphanBlockCacheManager orphanBlockCacheManager;

    @Override
    protected boolean check(SocketRequest<Block> request) {
        Block block = request.getData();
        String hash = block.getHash();

        //1. check: isGenesisBlock
        boolean isGenesisBlock = blockChainService.isGenesisBlock(block);
        if (isGenesisBlock) {
            return false;
        }

        //2. check: base info
        boolean isBasicValid = blockChainService.checkBlockBasicInfo(block);
        if (!isBasicValid) {
            LOGGER.error("error basic info block: ", block.getSimpleInfo());
            return false;
        }

        //3.check: exist
        boolean isExist = orphanBlockCacheManager.isContains(hash) || blockChainService.isExistBlock(hash);
        if (isExist) {
            LOGGER.info("the block is exist: ", block.getSimpleInfo());
            return false;
        }

        //4. check: producer stake
        boolean producerValid = blockChainService.checkBlockProducer(block);
        if (!producerValid) {
            LOGGER.error("the block produce stack is error: ", block.getSimpleInfo());
            return false;
        }

        //5.check: witness signatures
        boolean validWitnessSignature = blockChainService.checkWitnessSignature(block);
        if (!validWitnessSignature) {
            LOGGER.error("the block witness sig is error: ", block.getSimpleInfo());
            return false;
        }

        //6.check: orphan block
        boolean isOrphanBlock = blockChainService.isExistPreBlock(hash);
        if (isOrphanBlock) {
            BlockFullInfo blockFullInfo = new BlockFullInfo(block.getVersion(), request.getSourceId(), block);
            orphanBlockCacheManager.putAndRequestPreBlocks(blockFullInfo);
            LOGGER.warn("it is orphan block: ", block.getSimpleInfo());
            return false;
        }

        //7. check: transactions
        boolean validTransactions = blockChainService.checkTransactions(block);
        if (!validTransactions) {
            LOGGER.error("the block transactions are error: ", block.getSimpleInfo());
            return false;
        }
        return true;
    }

    @Override
    protected void process(SocketRequest<Block> request) {
        Block data = request.getData();
        long height = data.getHeight();
        String hash = data.getHash();
        boolean success = blockService.persistBlockAndIndex(data, data.getVersion());
        LOGGER.info("persisted block all info, success={},height={},block={}", success, height, hash);
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
        messageCenter.broadcast(new String[]{sourceId}, inventory);
        LOGGER.info("after persisted block, broadcast block : " + inventory);
    }
}
