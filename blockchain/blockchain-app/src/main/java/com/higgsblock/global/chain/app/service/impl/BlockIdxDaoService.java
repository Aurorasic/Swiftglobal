package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.formatter.BlockFormatter;
import com.higgsblock.global.chain.app.dao.entity.BlockEntity;
import com.higgsblock.global.chain.app.dao.entity.BlockIndexEntity;
import com.higgsblock.global.chain.app.dao.impl.BlockEntityDao;
import com.higgsblock.global.chain.app.dao.impl.BlockIndexEntityDao;
import com.higgsblock.global.chain.app.service.IBlockIndexService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhao xiaogang
 * @date 2018-05-22
 */
@Service
@Slf4j
public class BlockIdxDaoService implements IBlockIndexService {

    @Autowired
    private BlockEntityDao blockDao;

    @Autowired
    private BlockIndexEntityDao blockIndexDao;

    @Autowired
    private TransDaoService transDaoService;

    @Autowired
    private BlockFormatter blockFormatter;

    @Override
    public void addBlockIndex(Block block, Block toBeBestBlock) {
//        BlockIndex blockIndex;
//        ArrayList<String> blockHashes = new ArrayList<String>(1);
//        if (block.isGenesisBlock()) {
//            blockHashes.add(block.getHash());
//            blockIndex = new BlockIndex(1, blockHashes, 0);
//            needBuildUTXO = true;
//        } else {
//            blockIndex = getBlockIndexByHeight(block.getHeight());
//            boolean hasOldBest = blockIndex == null ? false : blockIndex.hasBestBlock();
//            boolean isBest = StringUtils.equals(bestBlockHash, block.getHash()) ? true : false;
//
//            if (blockIndex == null) {
//                blockHashes.add(block.getHash());
//                blockIndex = new BlockIndex(block.getHeight(), blockHashes, isBest ? 0 : -1);
//            } else {
//                blockIndex.addBlockHash(block.getHash(), isBest);
//                blockIndex.setBestHash(bestBlockHash);
//            }
//
//            boolean hasNewBest = blockIndex.hasBestBlock();
//            needBuildUTXO = !hasOldBest && hasNewBest;
//        }

        //insert BlockIndexEntity to sqlite DB
        //insertBatch(block, blockIndex);

        //modify by Huangshengli 2018-07-02
        insertBlockIndex(block);
        if (toBeBestBlock != null) {
            updateBestBlockIndex(toBeBestBlock);
        }

        if (block.isGenesisBlock()) {
            transDaoService.addTransIdxAndUtxo(block, block.getHash());
        } else {
            if (toBeBestBlock != null) {
                transDaoService.addTransIdxAndUtxo(toBeBestBlock, toBeBestBlock.getHash());
            }
        }
    }

    private void insertBlockIndex(Block block) {
        BlockIndexEntity blockIndexDO = new BlockIndexEntity();
        blockIndexDO.setBlockHash(block.getHash());
        blockIndexDO.setHeight(block.getHeight());
        blockIndexDO.setIsBest(block.isGenesisBlock() ? 0 : -1);
        blockIndexDO.setMinerAddress(block.getMinerFirstPKSig().getAddress());
        blockIndexDao.add(blockIndexDO);
        LOGGER.info("persisted block index: {}", blockIndexDO);
    }

    private void updateBestBlockIndex(Block bestBlock) {
        BlockIndex blockIndex = getBlockIndexByHeight(bestBlock.getHeight());
        for (int i = 0; i < blockIndex.getBlockHashs().size(); i++) {
            if (bestBlock.getHash().equals(blockIndex.getBlockHashs().get(i))) {
                BlockIndexEntity blockIndexEntity = blockIndexDao.getByBlockHash(bestBlock.getHash());
                blockIndexEntity.setIsBest(i);
                blockIndexDao.update(blockIndexEntity);
                LOGGER.info("persisted bestblock index: {}", blockIndexEntity);
                return;
            }
        }
    }

    @Override
    public BlockIndex getBlockIndexByHeight(long height) {
        List<BlockIndexEntity> blockIndexEntities = blockIndexDao.getAllByHeight(height);

        if (CollectionUtils.isNotEmpty(blockIndexEntities)) {
            BlockIndex blockIndex = new BlockIndex();
            blockIndex.setHeight(blockIndexEntities.get(0).getHeight());
            ArrayList<String> blockHashs = Lists.newArrayList();
            blockIndex.setBlockHashs(blockHashs);
            blockIndex.setBestIndex(-1);
            blockIndexEntities.forEach(blockIndexEntity -> {
                blockIndex.getBlockHashs().add(blockIndexEntity.getBlockHash());
                if (blockIndexEntity.getIsBest() != -1) {
                    blockIndex.setBestIndex(blockIndexEntity.getIsBest());
                }
            });
            return blockIndex;
        }
        LOGGER.info("get blockIndex is null by height = " + height);
        return null;
    }

    private void insertBatch(Block block, BlockIndex blockIndex) {
        long blockHeight = blockIndex.getHeight();
        String minerAddress = block.getMinerFirstPKSig().getAddress();
        ArrayList<String> blockHashs = blockIndex.getBlockHashs();
        String bestBlockHash = blockIndex.getBestBlockHash();
        List<BlockIndexEntity> blockIndexEntities = Lists.newArrayList();
        for (int i = 0; i < blockHashs.size(); i++) {
            String blockHash = blockHashs.get(i);
            BlockIndexEntity blockIndexEntity = new BlockIndexEntity();
            blockIndexEntity.setHeight(blockHeight);
            blockIndexEntity.setBlockHash(blockHash);
            if (StringUtils.equals(blockHash, bestBlockHash)) {
                blockIndexEntity.setIsBest(i);
                blockIndexEntity.setMinerAddress(minerAddress);
            } else {
                blockIndexEntity.setIsBest(-1);
                Block block1 = getBlockEntity2Block(blockHash);
                blockIndexEntity.setMinerAddress(block1.getMinerFirstPKSig().getAddress());
            }
            blockIndexEntities.add(blockIndexEntity);
        }
        blockIndexDao.insertBatch(blockIndexEntities);
    }

    private Block getBlockEntity2Block(String blockHash) {
        BlockEntity blockEntity = blockDao.getByField(blockHash);
        if (blockEntity == null) {
            return null;
        }
        return blockFormatter.parse(blockEntity.getData());
    }

    public BlockIndex getLastBlockIndex() {
        long maxHeight = blockIndexDao.getMaxHeight();
        return getBlockIndexByHeight(maxHeight);
    }

    public List<String> getLastHightBlockHashs() {
        List<String> result = getLastBlockIndex().getBlockHashs();
        if (CollectionUtils.isEmpty(result)) {
            throw new RuntimeException("error getLastHightBlockHashs" + getLastBlockIndex());
        }
        return result;
    }
}
