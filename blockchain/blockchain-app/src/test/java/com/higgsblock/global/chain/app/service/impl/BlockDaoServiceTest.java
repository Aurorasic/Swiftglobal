package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.ImmutableList;
import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockCacheManager;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.consensus.MinerScoreStrategy;
import com.higgsblock.global.chain.app.consensus.NodeManager;
import com.higgsblock.global.chain.app.dao.BlockDao;
import com.higgsblock.global.chain.app.dao.BlockIndexDao;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import com.higgsblock.global.chain.app.dao.entity.BlockIndexDaoEntity;
import com.higgsblock.global.chain.network.PeerManager;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.rocksdb.RocksDBException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author yangshenghong
 * @date 2018-06-26
 */
@PrepareForTest({MinerScoreStrategy.class})
public class BlockDaoServiceTest extends BaseMockTest {
    @Mock
    private BlockDao blockDao;

    @Mock
    private BlockIndexDao blockIndexDao;

    @Mock
    private BlockCacheManager blockCacheManager;

    @Mock
    private BlockIdxDaoService blockIdxDaoService;

    @Mock
    private TransDaoService transDaoService;

    @Mock
    private NodeManager nodeManager;

    @Mock
    private PeerManager peerManager;

    @InjectMocks
    private BlockDaoService blockDaoService;


    @Test
    public void isExistInDB() throws Exception {
        long height = 100;
        String blockHash = "blockHash";
        //when blockIndex == null
        PowerMockito.when(blockIndexDao.get(height)).thenReturn(null);
        Assert.assertFalse(blockDaoService.isExistInDB(height, blockHash));
        //when blockIndex != null
        BlockIndex blockIndex = new BlockIndex();
        PowerMockito.when(blockIndexDao.get(height)).thenReturn(blockIndex);
        blockIndex.setBestIndex(0);
        ArrayList<String> strings = new ArrayList<>();
        strings.add(blockHash);
        strings.add("blockHash2");
        strings.add("blockHash3");
        blockIndex.setBlockHashs(strings);
        //when exception happen
        Assert.assertTrue(blockDaoService.isExistInDB(height, blockHash));
        PowerMockito.when(blockIndexDao.get(height)).thenThrow(new RocksDBException(""));
        try {
            blockDaoService.isExistInDB(height, blockHash);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("Get block index error"));
        }
    }

    @Test
    public void isExist() throws Exception {
        Block block = new Block();
        PowerMockito.when(blockIndexDao.get(block.getHeight())).thenReturn(null);
        PowerMockito.when(blockCacheManager.isContains(block.getHash())).thenReturn(false);
        Assert.assertFalse(blockDaoService.isExist(block));
        //when blockIndex != null
        BlockIndex blockIndex = new BlockIndex();
        PowerMockito.when(blockIndexDao.get(block.getHeight())).thenReturn(blockIndex);
        PowerMockito.when(blockCacheManager.isContains(block.getHash())).thenReturn(true);
        blockIndex.setBestIndex(0);
        ArrayList<String> strings = new ArrayList<>();
        strings.add(block.getHash());
        strings.add("blockHash2");
        strings.add("blockHash3");
        blockIndex.setBlockHashs(strings);
        Assert.assertTrue(blockDaoService.isExist(block));

        //when blockIndex == null
        PowerMockito.when(blockIndexDao.get(block.getHeight())).thenReturn(null);
        PowerMockito.when(blockCacheManager.isContains(block.getHash())).thenReturn(true);
        Assert.assertTrue(blockDaoService.isExist(block));

        //when exception happen
        PowerMockito.when(blockIndexDao.get(block.getHeight())).thenThrow(new RocksDBException(""));
        PowerMockito.when(blockCacheManager.isContains(block.getHash())).thenReturn(false);
        try {
            blockDaoService.isExist(block);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("Get block index error"));
        }
    }

    @Test
    public void preIsExistInDB() throws Exception {
        Block block = new Block();
        PowerMockito.when(blockDao.get(block.getPrevBlockHash())).thenReturn(null);
        Assert.assertFalse(blockDaoService.preIsExistInDB(block));
        PowerMockito.when(blockDao.get(block.getPrevBlockHash())).thenReturn(block);
        Assert.assertTrue(blockDaoService.preIsExistInDB(block));
        PowerMockito.when(blockDao.get(block.getPrevBlockHash())).thenThrow(new RocksDBException(""));
        try {
            blockDaoService.preIsExistInDB(block);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("Get block error"));
        }
    }

    @Test
    public void getBlockByHash() throws Exception {
        String blockHash = "blockHash";
        Block block = new Block();
        block.setHash(blockHash);
        PowerMockito.when(blockDao.get(blockHash)).thenReturn(block);
        Block blockByHash = blockDaoService.getBlockByHash(blockHash);
        Assert.assertEquals(blockHash, blockByHash.getHash());
        PowerMockito.when(blockDao.get(blockHash)).thenThrow(new RocksDBException(""));
        try {
            blockDaoService.getBlockByHash(blockHash);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("Get block error"));
        }
    }

    @Test
    public void getBlocksByHeight() throws Exception {
        long height = 100L;
        PowerMockito.when(blockIndexDao.get(height)).thenReturn(null);
        Assert.assertFalse(CollectionUtils.isNotEmpty(blockDaoService.getBlocksByHeight(height)));

        BlockIndex blockIndex = new BlockIndex();
        ArrayList<String> strings = new ArrayList<>();
        strings.add("blockHash");
        strings.add("blockHash1");
        blockIndex.setBlockHashs(strings);
        PowerMockito.when(blockIndexDao.get(height)).thenReturn(blockIndex);
        Block block = new Block();
        PowerMockito.when(blockDao.get(anyString())).thenReturn(block);
        Assert.assertTrue(CollectionUtils.isNotEmpty(blockDaoService.getBlocksByHeight(height)));
        Assert.assertEquals(block, blockDaoService.getBlocksByHeight(height).get(0));
        PowerMockito.when(blockDao.get(anyString())).thenThrow(new RocksDBException(""));
        try {
            blockDaoService.getBlocksByHeight(height);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("Get blocks by height error"));
        }
        PowerMockito.when(blockIndexDao.get(height)).thenThrow(new RocksDBException(""));
        try {
            blockDaoService.getBlocksByHeight(height);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("Get block index error"));
        }
    }

    @Test
    public void getBlocksExcept() throws Exception {
        long height = 100L;
        String exceptBlockHash = "exceptBlockHash";
        BlockIndex blockIndex = new BlockIndex();
        PowerMockito.when(blockIndexDao.get(height)).thenReturn(blockIndex);
        ArrayList<String> strings = new ArrayList<>();
        strings.add(exceptBlockHash);
        strings.add("blockHash1");
        blockIndex.setBlockHashs(strings);
        Block block = new Block();
        PowerMockito.when(blockDao.get(anyString())).thenReturn(block);
        Assert.assertEquals(block, blockDaoService.getBlocksExcept(height, exceptBlockHash).get(0));

        PowerMockito.when(blockDao.get(anyString())).thenThrow(new RocksDBException(""));
        try {
            blockDaoService.getBlocksExcept(height, exceptBlockHash);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("Get block error"));
        }
        PowerMockito.when(blockIndexDao.get(height)).thenThrow(new RocksDBException(""));
        try {
            blockDaoService.getBlocksExcept(height, exceptBlockHash);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("Get block index error"));
        }
    }

    @Test
    public void getBestBlockByHeight() throws Exception {
        long height = 100L;
        BlockIndex blockIndex = new BlockIndex();
        PowerMockito.when(blockIndexDao.get(height)).thenReturn(blockIndex);
        blockIndex.setBestIndex(0);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("blockHash1");
        strings.add("blockHash2");
        strings.add("blockHash3");
        blockIndex.setBlockHashs(strings);
        Block block = new Block();
        PowerMockito.when(blockDao.get(blockIndex.getBestBlockHash())).thenReturn(block);
        Assert.assertTrue(blockDaoService.getBestBlockByHeight(height) != null);

        PowerMockito.when(blockIndexDao.get(height)).thenReturn(null);
        Assert.assertEquals(null, blockDaoService.getBestBlockByHeight(height));

        BlockIndex blockIndex1 = new BlockIndex();
        PowerMockito.when(blockIndexDao.get(height)).thenReturn(blockIndex1);
        blockIndex1.setBlockHashs(new ArrayList<>());
        Assert.assertEquals(null, blockDaoService.getBestBlockByHeight(height));

        BlockIndex blockIndex2 = new BlockIndex();
        PowerMockito.when(blockIndexDao.get(height)).thenReturn(blockIndex2);
        blockIndex2.setBestIndex(0);
        ArrayList<String> strings2 = new ArrayList<>();
        strings2.add("blockHash1");
        strings2.add("blockHash2");
        strings2.add("blockHash3");
        blockIndex2.setBlockHashs(strings);
        PowerMockito.when(blockDao.get(anyString())).thenThrow(new RocksDBException(""));
        try {
            blockDaoService.getBestBlockByHeight(height);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("Get block error"));
        }

        PowerMockito.when(blockIndexDao.get(height)).thenThrow(new RocksDBException(""));
        try {
            blockDaoService.getBestBlockByHeight(height);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("Get block index error"));
        }
    }

    @Test
    public void saveBlockCompletely() throws Exception {
        Block block = new Block();
        String blockHash = "block";
        BaseDaoEntity baseDaoEntity = new BaseDaoEntity(blockHash, block, "block");
        PowerMockito.when(blockDao.getEntity(blockHash, block)).thenReturn(baseDaoEntity);
        PowerMockito.doNothing().when(blockDao).writeBatch(baseDaoEntity);
        BlockIndexDaoEntity blockIndexDaoEntity = new BlockIndexDaoEntity();
        PowerMockito.when(blockIdxDaoService.addBlockIndex(block, blockHash)).thenReturn(blockIndexDaoEntity);
        BlockIndex blockIndex = new BlockIndex();
        BaseDaoEntity baseDaoEntity1 = new BaseDaoEntity(blockIndex.getHeight(), blockIndex, "blockIndex");
        BlockIndex blockIndex2 = new BlockIndex();
        BaseDaoEntity baseDaoEntity2 = new BaseDaoEntity(blockIndex2.getHeight(), blockIndex2, "blockIndex");
        List<BaseDaoEntity> baseDaoEntitys = new ArrayList<>();
        baseDaoEntitys.add(baseDaoEntity1);
        baseDaoEntitys.add(baseDaoEntity2);
        blockIndexDaoEntity.setBaseDaoEntity(baseDaoEntitys);
        blockIndexDaoEntity.setCreateUtxo(true);
        PowerMockito.doNothing().when(blockDao).writeBatch(blockIndexDaoEntity.getBaseDaoEntity());
        List<BaseDaoEntity> entityList = new ArrayList<>();
        PowerMockito.when(transDaoService.addTransIdxAndUtxo(block, blockHash)).thenReturn(entityList);
        PowerMockito.doNothing().when(blockDao).writeBatch(entityList);
        PowerMockito.mockStatic(MinerScoreStrategy.class);
        List<BaseDaoEntity> entityLists = new ArrayList<>();
        PowerMockito.when(MinerScoreStrategy.refreshMinersScore(block)).thenReturn(entityLists);
        List<BaseDaoEntity> entities = new ArrayList<>();
        PowerMockito.doNothing().when(blockDao).writeBatch(entities);
        BaseDaoEntity baseDaoEntity3 = new BaseDaoEntity(blockHash, block, "block");
        PowerMockito.when(nodeManager.calculateDposNodes(block)).thenReturn(baseDaoEntity3);
        PowerMockito.doNothing().when(blockDao).writeBatch(ImmutableList.of(baseDaoEntity3));
        List<Transaction> transactions = new ArrayList<>();
        Transaction transaction = new Transaction();
        transaction.setHash("transactionHash1");
        Transaction transaction2 = new Transaction();
        transaction2.setHash("transactionHash2");
        transactions.add(transaction);
        transactions.add(transaction2);
        block.setTransactions(transactions);
        long sn = 0;
        PowerMockito.when(nodeManager.getSn(block.getHeight() + 1)).thenReturn(sn);
        List<String> dposGroupBySn = new LinkedList<>();
        List<String> dpos = new ArrayList<>();
        PowerMockito.when(nodeManager.getDposGroupBySn(sn)).thenReturn(dpos);
        dpos.add("a");
        List<String> dpos2 = new ArrayList<>();
        PowerMockito.when(nodeManager.getDposGroupBySn(sn + 1)).thenReturn(dpos2);
        dpos2.add("b");
        PowerMockito.doNothing().when(peerManager).setMinerAddresses(dposGroupBySn);
        blockDaoService.saveBlockCompletely(block, blockHash);
    }

    @Test
    public void printAllBlockData() throws Exception {
        blockDaoService.printAllBlockData();
    }

    @Test
    public void checkBlockNumbers() throws Exception {
        List<byte[]> bytes = new ArrayList<>();
        PowerMockito.when(blockDao.keys()).thenReturn(bytes);
        Assert.assertTrue(blockDaoService.checkBlockNumbers());
    }
}