package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.ImmutableList;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockCacheManager;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionCacheManager;
import com.higgsblock.global.chain.app.consensus.MinerScoreStrategy;
import com.higgsblock.global.chain.app.consensus.NodeManager;
import com.higgsblock.global.chain.app.dao.BlockDao;
import com.higgsblock.global.chain.app.dao.BlockIndexDao;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import com.higgsblock.global.chain.app.dao.entity.BlockIndexDaoEntity;
import com.higgsblock.global.chain.app.service.IBlockService;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Zhao xiaogang
 * @date 2018-05-21
 */
@Service
@Slf4j
public class BlockDaoService implements IBlockService {

    @Autowired
    private BlockDao blockDao;

    @Autowired
    private BlockIndexDao blockIndexDao;

    @Autowired
    private BlockCacheManager blockCacheManager;

    @Autowired
    private BlockIdxDaoService blockIdxDaoService;

    @Autowired
    private TransDaoService transDaoService;

    @Autowired
    private TransactionCacheManager txCacheManager;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private PeerManager peerManager;

    @Override
    public boolean isExistInDB(long height, String blockHash) {
        try {
            BlockIndex blockIndex = blockIndexDao.get(height);
            return null != blockIndex
                    && null != blockIndex.getBestBlockHash()
                    && blockIndex.getBestBlockHash().equals(blockHash);
        } catch (RocksDBException e) {
            throw new IllegalStateException("Get block index error");
        }
    }

    @Override
    public boolean isExist(Block block) {
        try {
            BlockIndex blockIndex = blockIndexDao.get(block.getHeight());
            if (null != blockIndex
                    && null != blockIndex.getBestBlockHash()
                    && blockIndex.getBestBlockHash().equals(block.getHash())) {
                return true;
            }
        } catch (RocksDBException e) {
            throw new IllegalStateException("Get block index error");
        }

        if (blockCacheManager.isContains(block.getHash())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean preIsExistInDB(Block block) {
        try {
            if (blockDao.get(block.getPrevBlockHash()) != null) {
                return true;
            }
        } catch (RocksDBException e) {
            throw new IllegalStateException("Get block error");
        }
        return false;
    }

    @Override
    public Block getBlockByHash(String blockHash) {
        try {
            return blockDao.get(blockHash);
        } catch (RocksDBException e) {
            throw new IllegalStateException("Get block error");
        }
    }

    @Override
    public List<Block> getBlocksByHeight(long height) {
        BlockIndex blockIndex;
        try {
            blockIndex = blockIndexDao.get(height);
        } catch (Exception e) {
            throw new IllegalStateException("Get block index error");
        }

        List<Block> blocks = new LinkedList<>();
        if (blockIndex != null) {
            ArrayList<String> blockHashes = blockIndex.getBlockHashs();

            blockHashes.forEach(blockHash -> {
                Block otherBlock;
                try {
                    otherBlock = blockDao.get(blockHash);
                } catch (RocksDBException e) {
                    throw new IllegalStateException("Get blocks by height error");
                }

                blocks.add(otherBlock);
            });
        }

        return blocks;
    }

    @Override
    public List<Block> getBlocksExcept(long height, String exceptBlockHash) {
        BlockIndex blockIndex;
        try {
            blockIndex = blockIndexDao.get(height);
        } catch (RocksDBException e) {
            throw new IllegalStateException("Get block index error");
        }

        ArrayList<String> blockHashes = blockIndex.getBlockHashs();
        List<Block> blocks = new LinkedList<>();

        for (String blockHash : blockHashes) {
            if (StringUtils.equals(blockHash, exceptBlockHash)) {
                continue;
            }

            Block otherBlock;
            try {
                otherBlock = blockDao.get(blockHash);
            } catch (RocksDBException e) {
                throw new IllegalStateException("Get block error");
            }

            blocks.add(otherBlock);
        }
        return blocks;
    }

    @Override
    public Block getBestBlockByHeight(long height) {
        BlockIndex blockIndex;
        try {
            blockIndex = blockIndexDao.get(height);
        } catch (RocksDBException e) {
            throw new IllegalStateException("Get block index error");
        }

        if (blockIndex == null) {
            return null;
        }

        String bestBlockHash = blockIndex.getBestBlockHash();
        if (StringUtils.isEmpty(bestBlockHash)) {
            return null;
        }

        try {
            return blockDao.get(bestBlockHash);
        } catch (RocksDBException e) {
            throw new IllegalStateException("Get block error");
        }
    }

    /**
     * Steps for saving block completely:
     * 1.save block
     * 2.save block index
     * 3.save transaction index
     * 4.save utxo
     * 5.save score
     * 6.save new dpos
     * 7.refresh cache
     **/
    @Override
    public void saveBlockCompletely(Block block, String bestBlockHash) throws Exception {

        //step 1
        BaseDaoEntity blockDaoEntity = blockDao.getEntity(bestBlockHash, block);
        blockDao.writeBatch(blockDaoEntity);

        //step 2
        BlockIndexDaoEntity blockIndexDaoEntity = blockIdxDaoService.addBlockIndex(block, bestBlockHash);
        blockDao.writeBatch(blockIndexDaoEntity.getBaseDaoEntity());

        //step 3,4
        if (blockIndexDaoEntity.isCreateUtxo()) {
            List<BaseDaoEntity> entityList = transDaoService.addTransIdxAndUtxo(block, bestBlockHash);
            blockDao.writeBatch(entityList);
        }

        //step 5
        MinerScoreStrategy.refreshMinersScore(block);

        //step 6
        nodeManager.calculateDposNodes(block);
        if (entity != null) {
            blockDao.writeBatch(ImmutableList.of(entity));
        }

        //step 7
        refreshCache(bestBlockHash, block);

        List<String> dposGroupBySn = new LinkedList<>();
        long sn = nodeManager.getSn(block.getHeight() + 1);
        List<String> dpos = nodeManager.getDposGroupBySn(sn);
        if (!CollectionUtils.isEmpty(dpos)) {
            dposGroupBySn.addAll(dpos);
        }
        dpos = nodeManager.getDposGroupBySn(sn + 1);
        if (!CollectionUtils.isEmpty(dpos)) {
            dposGroupBySn.addAll(dpos);
        }
        peerManager.setMinerAddresses(dposGroupBySn);

    }

    @Override
    public void printAllBlockData() {
    }

    @Override
    public boolean checkBlockNumbers() {
        long blockIndexSize = 0L;
        long blockMapSize = blockDao.keys().size();

        if (blockMapSize < 0L || blockMapSize > Long.MAX_VALUE) {
            LOGGER.error("blockMapSize is error blockMapSize = {}", blockMapSize);
            return false;
        }

        //TODO: zhao xiaogang  should optimize  2018-05-22

        return true;
    }

    private void refreshCache(String bestBlockHash, Block block) {
        blockCacheManager.remove(bestBlockHash);

        block.getTransactions().stream().forEach(tx -> {
            txCacheManager.remove(tx.getHash());
        });
    }
}
