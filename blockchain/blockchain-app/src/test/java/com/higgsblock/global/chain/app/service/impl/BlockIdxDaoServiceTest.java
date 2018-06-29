package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.dao.BlockIndexDao;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import com.higgsblock.global.chain.app.dao.entity.BlockIndexDaoEntity;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.rocksdb.RocksDBException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-06-25
 */
@PrepareForTest({BlockIdxDaoService.class})
public class BlockIdxDaoServiceTest extends BaseMockTest {
    @Mock
    private BlockIndexDao blockIndexDao;
    @Mock
    private DictionaryService dictionaryService;
    @InjectMocks
    private BlockIdxDaoService blockIdxDaoService;

    @Test
    public void getBlockIndexByHeight() throws RocksDBException {
        long height = 123L;

        BlockIndex blockIndex = new BlockIndex();
        blockIndex.setHeight(123L);
        blockIndex.setBestHash("blockHash2");
        blockIndex.setBestIndex(1);
        ArrayList<String> blockHashs = Lists.newArrayList();
        blockHashs.add("blockHash1");
        blockHashs.add("blockHash2");
        blockHashs.add("blockHash3");
        blockIndex.setBlockHashs(blockHashs);
        PowerMockito.when(blockIndexDao.get(height)).thenReturn(blockIndex);

        BlockIndex result = blockIdxDaoService.getBlockIndexByHeight(height);
        Assert.assertEquals(123L, result.getHeight());
        Assert.assertEquals(1, result.getBestIndex());
        Assert.assertEquals("blockHash2", result.getBestBlockHash());
        Assert.assertEquals("blockHash1", blockIndex.getBlockHashs().get(0));
        Assert.assertEquals("blockHash2", blockIndex.getBlockHashs().get(1));
        Assert.assertEquals("blockHash3", blockIndex.getBlockHashs().get(2));

        height = 100L;
        try {
            PowerMockito.when(blockIndexDao.get(height)).thenThrow(new RocksDBException("Get block index error"));
            blockIdxDaoService.getBlockIndexByHeight(height);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("Get block index error"));
        }
    }

    @Test
    public void addBlockIndexWithFistBlock() throws Exception {
        BaseDaoEntity baseDaoEntity = PowerMockito.mock(BaseDaoEntity.class);
        BlockIndex mockBlockIndex = Mockito.spy(new BlockIndex());
        Block block = new Block();
        //first block
        block.setHeight(1);
        block.setHash("firstBlockHash");

        ArrayList<String> blockHashs = Lists.newArrayList();
        blockHashs.add(block.getHash());
        mockBlockIndex.setHeight(1L);
        mockBlockIndex.setBlockHashs(blockHashs);
        mockBlockIndex.setBestIndex(0);

        PowerMockito.whenNew(BlockIndex.class).withAnyArguments().thenReturn(mockBlockIndex);
        PowerMockito.when(blockIndexDao.getEntity(block.getHeight(), mockBlockIndex)).thenReturn(baseDaoEntity);
        BlockIndexDaoEntity entity = blockIdxDaoService.addBlockIndex(block, "bestBlockHash");
        Assert.assertEquals(baseDaoEntity, entity.getBaseDaoEntity().get(0));
        Assert.assertTrue(entity.isCreateUtxo());
    }

    @Test
    public void addBlockIndex() throws Exception {
        BaseDaoEntity baseDaoEntity = PowerMockito.mock(BaseDaoEntity.class);
        BaseDaoEntity entity = PowerMockito.mock(BaseDaoEntity.class);
        BlockIndex blockIndex = null;
        Block block = new Block();
        block.setHeight(2L);
        block.setHash("BlockHash");

        BlockIndex mockBlockIndex = Mockito.spy(new BlockIndex());
        ArrayList<String> blockHashs = Lists.newArrayList();
        blockHashs.add(block.getHash());
        mockBlockIndex.setHeight(2L);
        mockBlockIndex.setBlockHashs(blockHashs);
        mockBlockIndex.setBestIndex(-1);
        String bestBlockHash = "bestBlockHash";
        //blockIndex == null && isBeset = false
        PowerMockito.when(blockIndexDao.get(block.getHeight())).thenReturn(null);
        PowerMockito.whenNew(BlockIndex.class).withAnyArguments().thenReturn(mockBlockIndex);
        PowerMockito.when(blockIndexDao.getEntity(mockBlockIndex.getHeight(), mockBlockIndex)).thenReturn(baseDaoEntity);
        BlockIndexDaoEntity baseEntity1 = blockIdxDaoService.addBlockIndex(block, bestBlockHash);
        Assert.assertEquals(baseDaoEntity, baseEntity1.getBaseDaoEntity().get(0));
        Assert.assertFalse(baseEntity1.isCreateUtxo());
        //blockIndex == null && isBest = true
        block.setHash(bestBlockHash);
        mockBlockIndex.setBestIndex(0);
        PowerMockito.when(blockIndexDao.get(block.getHeight())).thenReturn(blockIndex);
        PowerMockito.whenNew(BlockIndex.class).withAnyArguments().thenReturn(mockBlockIndex);
        PowerMockito.when(dictionaryService.saveLatestBestBlockIndex(block.getHeight(), bestBlockHash)).thenReturn(entity);
        PowerMockito.when(blockIndexDao.getEntity(block.getHeight(), mockBlockIndex)).thenReturn(baseDaoEntity);
        BlockIndexDaoEntity baseEntity2 = blockIdxDaoService.addBlockIndex(block, bestBlockHash);
        Assert.assertEquals(entity, baseEntity2.getBaseDaoEntity().get(0));
        Assert.assertEquals(baseDaoEntity, baseEntity2.getBaseDaoEntity().get(1));
        Assert.assertTrue(baseEntity2.isCreateUtxo());
        //isBest is false and hasOldBest is true
        blockIndex = new BlockIndex();
        blockIndex.setBestIndex(0);
        block.setHash("blockHash");
        PowerMockito.when(blockIndexDao.get(block.getHeight())).thenReturn(blockIndex);
        BlockIndexDaoEntity baseEntity3 = blockIdxDaoService.addBlockIndex(block, bestBlockHash);
        Assert.assertEquals(baseDaoEntity, baseEntity3.getBaseDaoEntity().get(0));
        Assert.assertFalse(baseEntity3.isCreateUtxo());
    }

    @Test
    public void keys() {
        List<byte[]> expected = Lists.newArrayList();
        expected.add(new byte[]{'k', 'e', 'y', 's'});
        PowerMockito.when(blockIndexDao.keys()).thenReturn(expected);
        List<byte[]> result = blockIdxDaoService.keys();
        Assert.assertEquals(expected.size(), result.size());
        for (int i = 0; i < result.get(0).length; i++) {
            Assert.assertEquals(expected.get(0)[i], result.get(0)[i]);
        }
    }
}