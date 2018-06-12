package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.dao.BlockIndexDao;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import com.higgsblock.global.chain.app.dao.entity.BlockIndexDaoEntity;
import com.higgsblock.global.chain.app.service.IBlockIndexService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.rocksdb.RocksDBException;
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
    private BlockIndexDao blockIndexDao;

    @Autowired
    private DictionaryService dictionaryService;

    @Override
    public BlockIndex getBlockIndexByHeight(long height) {
        try {
            return blockIndexDao.get(height);
        } catch (RocksDBException e) {
            throw new IllegalStateException("Get block index error");
        }
    }

    @Override
    public BlockIndexDaoEntity  addBlockIndex(Block block, String bestBlockHash) throws Exception {
        boolean needBuildUTXO;
        BlockIndex blockIndex;
        ArrayList blockHashes = new ArrayList<String>(1);
        List<BaseDaoEntity> entityList = new ArrayList<>();

        if (block.isGenesisBlock()) {
            blockHashes.add(block.getHash());
            blockIndex = new BlockIndex(1, blockHashes, 0);
            needBuildUTXO = true;
        } else {
            blockIndex = blockIndexDao.get(block.getHeight());
            boolean hasOldBest = blockIndex == null ? false : blockIndex.hasBestBlock();
            boolean isBest = StringUtils.equals(bestBlockHash, block.getHash()) ? true : false;

            if (blockIndex == null) {
                blockHashes.add(block.getHash());
                blockIndex = new BlockIndex(block.getHeight(), blockHashes, isBest ? 0 : -1);
            } else {
                blockIndex.addBlockHash(block.getHash(), isBest);
                blockIndex.setBestHash(bestBlockHash);
            }

            if (isBest) {
                BaseDaoEntity entity = dictionaryService.saveLatestBestBlockIndex(block.getHeight(), bestBlockHash);
                entityList.add(entity);
            }
            boolean hasNewBest = blockIndex.hasBestBlock();
            needBuildUTXO = !hasOldBest && hasNewBest;
        }

        LOGGER.info("persisted block index: " + blockIndex.toString());

        BaseDaoEntity baseDaoEntity = blockIndexDao.getEntity(blockIndex.getHeight(), blockIndex);
        entityList.add(baseDaoEntity);
        BlockIndexDaoEntity entity = new BlockIndexDaoEntity();
        entity.setCreateUtxo(needBuildUTXO);
        entity.setBaseDaoEntity(entityList);

        return  entity;
    }

    @Override
    public List<byte[]> keys() {
        return blockIndexDao.keys();
    }

}
