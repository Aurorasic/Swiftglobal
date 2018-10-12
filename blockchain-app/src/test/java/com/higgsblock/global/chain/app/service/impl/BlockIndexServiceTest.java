package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.dao.IBlockIndexRepository;
import com.higgsblock.global.chain.app.dao.entity.BlockIndexEntity;
import com.higgsblock.global.chain.app.service.IBlockChainInfoService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-10-10
 */
@PrepareForTest
public class BlockIndexServiceTest extends BaseMockTest {
    @InjectMocks
    private BlockIndexService blockIndexService;

    @Spy
    @InjectMocks
    private BlockIndexService spyBlockIndexService = new BlockIndexService();

    @Mock
    private IBlockIndexRepository blockIndexRepository;
    @Mock
    private TransactionIndexService transactionIndexService;
    @Mock
    private IBlockChainInfoService blockChainInfoService;

    @Test
    public void addBlockIndex() {
        //when toBeBestBlock is null
        Block block = new Block();
        blockIndexService.addBlockIndex(block, null);

        //when toBeBestBlock is not null and The hash list in blockIndex does not contain the hash of the specified block
        Block toBeBestBlock = new Block();
        toBeBestBlock.setHeight(2L);
        BlockIndex blockIndex = new BlockIndex();
        Mockito.doReturn(blockIndex).when(spyBlockIndexService).getBlockIndexByHeight(toBeBestBlock.getHeight());
        ArrayList<String> hashs = new ArrayList<>(3);
        hashs.add("blockHash1");
        hashs.add("blockHash2");
        hashs.add("blockHash3");
        blockIndex.setBlockHashs(hashs);
        spyBlockIndexService.addBlockIndex(block, toBeBestBlock);

        //The hash list in blockIndex does contain the hash of the specified block
        toBeBestBlock.setHash("blockHash2");
        PowerMockito.when(blockIndexRepository.findByBlockHash(toBeBestBlock.getHash())).thenReturn(new BlockIndexEntity());
        spyBlockIndexService.addBlockIndex(block, toBeBestBlock);
    }

    @Test
    public void deleteByHeight() {
        long height = 2L;
        PowerMockito.when(blockIndexRepository.deleteByHeight(height)).thenReturn(1);
        Assert.assertEquals(blockIndexService.deleteByHeight(height), 1);
    }

    @Test
    public void getHeightByBlockHash() {
        //findByBlockHash return null
        String blockHash = "blockHash";
        PowerMockito.when(blockIndexRepository.findByBlockHash(blockHash)).thenReturn(null);
        Assert.assertNull(blockIndexService.getHeightByBlockHash(blockHash));

        //The result is queried and returned correctly
        BlockIndexEntity blockIndexEntity = new BlockIndexEntity();
        blockIndexEntity.setHeight(2L);
        PowerMockito.when(blockIndexRepository.findByBlockHash(blockHash)).thenReturn(blockIndexEntity);
        Assert.assertEquals(blockIndexService.getHeightByBlockHash(blockHash), Long.valueOf(blockIndexEntity.getHeight()));
    }

    @Test
    public void getBlockIndexByHeight() {
        long height = 2L;
        //returns empty according to the height query list
        PowerMockito.when(blockIndexRepository.findByHeight(height)).thenReturn(null);
        Assert.assertNull(blockIndexService.getBlockIndexByHeight(height));

        //not have best block
        List<BlockIndexEntity> blockIndexEntities = new ArrayList<>(2);
        BlockIndexEntity blockIndexEntity = new BlockIndexEntity(), blockIndexEntity1 = new BlockIndexEntity();
        blockIndexEntity.setIsBest(-1);
        blockIndexEntity1.setIsBest(-1);
        blockIndexEntities.add(blockIndexEntity1);
        blockIndexEntities.add(blockIndexEntity);
        PowerMockito.when(blockIndexRepository.findByHeight(height)).thenReturn(blockIndexEntities);
        Assert.assertEquals(blockIndexService.getBlockIndexByHeight(height).getHeight(), height);

        //existing bestBlock
        blockIndexEntity.setIsBest(2);
        Assert.assertEquals(blockIndexService.getBlockIndexByHeight(height).getHeight(), height);
    }

    @Test
    public void getLastBlockIndex() {
        long maxHeight = 100L;
        PowerMockito.when(blockChainInfoService.getMaxHeight()).thenReturn(maxHeight);
        BlockIndex blockIndex = new BlockIndex();
        blockIndex.setHeight(maxHeight);
        Mockito.doReturn(blockIndex).when(spyBlockIndexService).getBlockIndexByHeight(maxHeight);
        Assert.assertEquals(spyBlockIndexService.getLastBlockIndex().getHeight(), blockIndex.getHeight());
    }

    @Test
    public void getLastHeightBlockHashs() {
        //when hash list is empty
        BlockIndex blockIndex = new BlockIndex();
        Mockito.doReturn(blockIndex).when(spyBlockIndexService).getLastBlockIndex();
        try {
            spyBlockIndexService.getLastHeightBlockHashs();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
            Assert.assertTrue(e.getMessage().contains("error getLastHeightBlockHashs"));
        }

        //when hash list is not empty
        ArrayList<String> blockHashs = new ArrayList<>();
        blockHashs.add("hash1");
        blockHashs.add("hash2");
        blockIndex.setBlockHashs(blockHashs);
        Assert.assertEquals(spyBlockIndexService.getLastHeightBlockHashs(), blockHashs);
    }
}