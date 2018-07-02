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
    public void addBlockIndex(Block block, String bestBlockHash) {
        boolean needBuildUTXO = false;
        BlockIndex blockIndex;
        ArrayList<String> blockHashes = new ArrayList<String>(1);
        if (block.isGenesisBlock()) {
            blockHashes.add(block.getHash());
            blockIndex = new BlockIndex(1, blockHashes, 0);
            needBuildUTXO = true;
        } else {
            blockIndex = getBlockIndexByHeight(block.getHeight());
            boolean hasOldBest = blockIndex == null ? false : blockIndex.hasBestBlock();
            boolean isBest = StringUtils.equals(bestBlockHash, block.getHash()) ? true : false;

            if (blockIndex == null) {
                blockHashes.add(block.getHash());
                blockIndex = new BlockIndex(block.getHeight(), blockHashes, isBest ? 0 : -1);
            } else {
                blockIndex.addBlockHash(block.getHash(), isBest);
                blockIndex.setBestHash(bestBlockHash);
            }

            boolean hasNewBest = blockIndex.hasBestBlock();
            needBuildUTXO = !hasOldBest && hasNewBest;
        }

        //insert BlockIndexEntity to sqlite DB
        insertBatch(block, blockIndex);
        LOGGER.info("persisted block index: " + blockIndex.toString());

        if (needBuildUTXO) {
            transDaoService.addTransIdxAndUtxo(block, bestBlockHash);
        }
    }

    @Override
    public BlockIndex getBlockIndexByHeight(long height) {
        List<BlockIndexEntity> blockIndexEntities = blockIndexDao.getAllByHeight(height);
        ArrayList<String> blockHashs = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(blockIndexEntities)) {
            blockIndexEntities.forEach(blockIdxEntity -> {
                blockHashs.add(blockIdxEntity.getBlockHash());
            });
            BlockIndex blockIndex = new BlockIndex();
            blockIndexEntities.forEach(blockIndexEntity -> {
                if (blockIndexEntity.getIsBest() != -1) {
                    blockIndex.setHeight(blockIndexEntity.getHeight());
                    blockIndex.setBestIndex(blockIndexEntity.getIsBest());
                    blockIndex.setBlockHashs(blockHashs);
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
}
