package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.config.AppConfig;
import com.higgsblock.global.chain.app.service.*;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author Su Jiulong
 * @date 2018/9/25
 */
public class BlockChainServiceTest extends BaseMockTest {

    @InjectMocks
    private BlockChainService blockChainService;

    @Mock
    private ITransactionService transactionService;

    @Mock
    private AppConfig config;

    @Mock
    private IBlockService blockService;

    @Mock
    private IDposService dposService;

    @Mock
    private IWitnessService witnessService;

    @Mock
    private WitnessTimer witnessTimer;

    @Mock
    private IBlockChainInfoService blockChainInfoService;

    @Mock
    private IBlockIndexService blockIndexService;

    @Test
    public void isDposMiner() {
        List<String> dposList = new ArrayList<>(4);
        String dops = "dpos";
        for (int i = 0; i < 4; i++) {
            dposList.add(dops + i);
        }

        //dposList is not empty and it contains dpos3
        String dpos3 = "dpos3";
        String preBlockHash = "preBlockHash";
        PowerMockito.when(dposService.getRestDposMinersByPreHash(preBlockHash)).thenReturn(dposList);
        Assert.assertTrue(blockChainService.isDposMiner(dpos3, preBlockHash));

        //dposList is not empty and it not contains dpos3
        dpos3 = "not contains";
        Assert.assertFalse(blockChainService.isDposMiner(dpos3, preBlockHash));

        //dposList is null
        PowerMockito.when(dposService.getRestDposMinersByPreHash(preBlockHash)).thenReturn(null);
        Assert.assertFalse(blockChainService.isDposMiner(dpos3, preBlockHash));

        //dposList is empty
        PowerMockito.when(dposService.getRestDposMinersByPreHash(preBlockHash)).thenReturn(Collections.emptyList());
        Assert.assertFalse(blockChainService.isDposMiner(dpos3, preBlockHash));
    }

    @Test
    public void isMinerOnBest() {
        String miner = "isMinser";
        PowerMockito.when(transactionService.hasStakeOnBest(miner, SystemCurrencyEnum.MINER)).thenReturn(true);
        Assert.assertTrue(blockChainService.isMinerOnBest(miner));

        miner = "isNotMiner";
        PowerMockito.when(transactionService.hasStakeOnBest(miner, SystemCurrencyEnum.MINER)).thenReturn(false);
        Assert.assertFalse(blockChainService.isMinerOnBest(miner));
    }

    @Test
    public void isWitness() {
        long height = 100L;
        String address = "isWitness";
        PowerMockito.when(witnessService.isWitness(anyString())).thenReturn(true);
        Assert.assertTrue(blockChainService.isWitness(address, height));

        address = "isNotWitness";
        PowerMockito.when(witnessService.isWitness(anyString())).thenReturn(false);
        Assert.assertFalse(blockChainService.isWitness(address, height));
    }

    @Test
    public void isGuarder() {
        String preBlockHash = "preBlockHash";
        String address = "isGuarder";
        PowerMockito.when(transactionService.hasStake(preBlockHash, address, SystemCurrencyEnum.GUARDER)).thenReturn(true);
        Assert.assertTrue(blockChainService.isGuarder(address, preBlockHash));

        address = "isNotGuarder";
        PowerMockito.when(transactionService.hasStake(preBlockHash, address, SystemCurrencyEnum.GUARDER)).thenReturn(false);
        Assert.assertFalse(blockChainService.isGuarder(address, preBlockHash));
    }

    @Test
    public void isGenesisBlock() {
        Block block = null;
        Assert.assertFalse(blockChainService.isGenesisBlock(block));

        block = new Block();
        block.setHeight(2L);
        Assert.assertFalse(blockChainService.isGenesisBlock(block));

        block.setHeight(1L);
        block.setPrevBlockHash("is not null");
        Assert.assertFalse(blockChainService.isGenesisBlock(block));

        String blockHash = "blockHash";
        block.setPrevBlockHash(null);
        block.setHash(blockHash);
        PowerMockito.when(config.getGenesisBlockHash()).thenReturn(blockHash);
        Assert.assertTrue(blockChainService.isGenesisBlock(block));
    }

    @Test
    public void isGenesisBlock1() {
        String blockHash = "blockHash";
        PowerMockito.when(config.getGenesisBlockHash()).thenReturn(blockHash);
        Assert.assertTrue(blockChainService.isGenesisBlock(blockHash));
    }

    @Test
    public void isExistBlock() {
        Block block = new Block();
        PowerMockito.when(blockService.getBlockByHash(anyString())).thenReturn(block).thenReturn(null);
        String blockHash = "blockHash";
        Assert.assertTrue(blockChainService.isExistBlock(blockHash));
        Assert.assertFalse(blockChainService.isExistBlock(blockHash));
    }

    @Test
    public void checkBlockBasicInfo() {
        Block block = new Block();
        Assert.assertFalse(blockChainService.checkBlockBasicInfo(block));
    }

    @Test
    public void checkTransactions() {
        Block block = new Block();
        PowerMockito.when(transactionService.validTransactions(block)).thenReturn(true);
        Assert.assertTrue(blockChainService.checkTransactions(block));

        PowerMockito.when(transactionService.validTransactions(block)).thenReturn(false);
        Assert.assertFalse(blockChainService.checkTransactions(block));
    }

    @Test
    public void checkWitnessSignature() {
        Block block = new Block();
        PowerMockito.when(blockService.checkWitnessSignatures(block)).thenReturn(true);
        Assert.assertTrue(blockChainService.checkWitnessSignature(block));

        PowerMockito.when(blockService.checkWitnessSignatures(block)).thenReturn(false);
        Assert.assertFalse(blockChainService.checkWitnessSignature(block));
    }

    @Test
    public void checkBlockProducer() {
        Block block = new Block();
        PowerMockito.when(blockService.checkDposProducerPermission(block)).thenReturn(true);
        Assert.assertTrue(blockChainService.checkBlockProducer(block));

        PowerMockito.when(blockService.checkDposProducerPermission(block)).thenReturn(false);
        PowerMockito.when(witnessTimer.checkGuarderPermissionWithoutTimer(block)).thenReturn(true).thenReturn(false);
        Assert.assertTrue(blockChainService.checkBlockProducer(block));
        Assert.assertFalse(blockChainService.checkBlockProducer(block));

    }

    @Test
    public void getMaxHeight() {
        long maxHeight = 100L;
        PowerMockito.when(blockChainInfoService.getMaxHeight()).thenReturn(maxHeight);
        Assert.assertEquals(maxHeight, blockChainService.getMaxHeight());
    }

    @Test
    public void getBestMaxHeight() {
        BlockIndex blockIndex = new BlockIndex();
        long height = 100L;
        blockIndex.setHeight(height);
        PowerMockito.when(blockService.getLastBestBlockIndex()).thenReturn(blockIndex);
        Assert.assertEquals(height, blockChainService.getBestMaxHeight());
    }

    @Test
    public void getBlock() {
        String blockHash = "blockHash";
        Block block = new Block();
        block.setHash(blockHash);
        PowerMockito.when(blockService.getBlockByHash(blockHash)).thenReturn(block);
        Assert.assertEquals(block, blockChainService.getBlock(blockHash));

        Assert.assertNotEquals(new Block(), blockChainService.getBlock(blockHash));
    }

    @Test
    public void getBlocks() {
        List<Block> blocks = new ArrayList<>(3);
        for (int i = 1; i <= 3; i++) {
            Block block = new Block();
            block.setHeight(i);
            block.setHash("blockHash" + i);
            blocks.add(block);
        }
        PowerMockito.when(blockService.getBlocksByHeight(anyLong())).thenReturn(blocks);
        Assert.assertEquals(blocks, blockChainService.getBlocks(100L));
    }

    @Test
    public void getBlockIndex() {
        BlockIndex blockIndex = new BlockIndex();
        PowerMockito.when(blockIndexService.getBlockIndexByHeight(anyLong())).thenReturn(blockIndex);
        Assert.assertEquals(blockIndex, blockChainService.getBlockIndex(100L));
    }

    @Test
    public void getHighestBlocks() {
        BlockIndex lastBlockIndex = new BlockIndex();
        long height = 100L;
        lastBlockIndex.setHeight(height);

        List<Block> blocks = new ArrayList<>(2);
        for (int i = 1; i <= 2; i++) {
            Block block = new Block();
            block.setHeight(i);
            block.setHash("blockHash" + i);
            blocks.add(block);
        }
        PowerMockito.when(blockIndexService.getLastBlockIndex()).thenReturn(lastBlockIndex);
        PowerMockito.when(blockService.getBlocksByHeight(anyLong())).thenReturn(blocks);
        Assert.assertEquals(blocks, blockChainService.getHighestBlocks());
    }
}