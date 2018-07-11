package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.dao.entity.BlockIndexEntity;
import com.higgsblock.global.chain.app.dao.impl.BlockIndexEntityDao;
import com.higgsblock.global.chain.app.service.IBlockIndexService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
    private BlockIndexEntityDao blockIndexDao;

    @Autowired
    private TransDaoService transDaoService;

    @Override
    public void addBlockIndex(Block block, Block toBeBestBlock) {
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
        LOGGER.info("get blockIndex is null by height={}", height);
        return null;
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
