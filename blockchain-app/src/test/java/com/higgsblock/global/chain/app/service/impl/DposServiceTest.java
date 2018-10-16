package com.higgsblock.global.chain.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.SignaturePair;
import com.higgsblock.global.chain.app.common.ScoreRangeEnum;
import com.higgsblock.global.chain.app.dao.IDposRepository;
import com.higgsblock.global.chain.app.dao.entity.DposEntity;
import com.higgsblock.global.chain.app.service.IDposService;
import com.higgsblock.global.chain.app.service.IScoreService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;

/**
 * @author Su Jiulong
 * @date 2018/10/12
 */
@PrepareForTest({DposService.class})
public class DposServiceTest extends BaseMockTest {

    @InjectMocks
    private IDposService dposService = new DposService();

    @InjectMocks
    @Spy
    private IDposService spyDposService = new DposService();

    @Mock
    private IDposRepository dposRepository;

    @Mock
    private IScoreService scoreService;

    @Mock
    private BlockService blockService;

    private static final long SN = 4;

    @Test
    public void get() {
        //find by sn return null
        PowerMockito.when(dposRepository.findBySn(SN)).thenReturn(null);
        Assert.assertEquals(null, dposService.get(SN));

        PowerMockito.when(dposRepository.findBySn(SN)).thenReturn(getDposEntity());
        List<String> result = dposService.get(SN);
        Assert.assertEquals(getTestAddresses("address", 3), result);
    }

    @Test
    public void save() {
        DposEntity dposEntity = getDposEntity();
        PowerMockito.when(dposRepository.save(dposEntity)).thenReturn(dposEntity);
        dposService.save(SN, getTestAddresses("address", 3));
    }

    @Test
    public void calcNextDposNodes() {
        long maxBlockHeight = 13L;
        Block toBeBestBlock = new Block();
        //the height is not first block of round
        toBeBestBlock.setHeight(10L);
        List<String> dposAddresses = dposService.calcNextDposNodes(toBeBestBlock, maxBlockHeight);
        Assert.assertTrue(CollectionUtils.isEmpty(dposAddresses));

        //the sn has selected dposAddress
        toBeBestBlock.setHeight(7L);
        PowerMockito.when(dposRepository.findBySn(anyLong())).thenReturn(new DposEntity());
        dposAddresses = dposService.calcNextDposNodes(toBeBestBlock, maxBlockHeight);
        Assert.assertTrue(CollectionUtils.isEmpty(dposAddresses));

        //the sn has not selected dposAddress
        PowerMockito.when(dposRepository.findBySn(anyLong())).thenReturn(null);
        PowerMockito.when(dposRepository.findBySn(SN)).thenReturn(getDposEntity());
        List<String> currentGroup = getTestAddresses("address", 3);

        //seven dops nodes were selected as required.
        List<String> level5List = getTestAddresses("level_5_List", 10);
        List<String> level4List = getTestAddresses("level_4_List", 8);
        List<String> level3List = getTestAddresses("level_3_List", 5);
        List<String> level2List = getTestAddresses("level_2_List", 5);
        List<String> level1List = getTestAddresses("level_1_List", 5);
        PowerMockito.when(scoreService.queryAddresses(ScoreRangeEnum.LEVEL5_SCORE, currentGroup)).thenReturn(level5List);
        PowerMockito.when(scoreService.queryAddresses(ScoreRangeEnum.LEVEL4_SCORE, currentGroup)).thenReturn(level4List);
        PowerMockito.when(scoreService.queryAddresses(ScoreRangeEnum.LEVEL3_SCORE, currentGroup)).thenReturn(level3List);
        PowerMockito.when(scoreService.queryAddresses(ScoreRangeEnum.LEVEL2_SCORE, currentGroup)).thenReturn(level2List);
        PowerMockito.when(scoreService.queryAddresses(ScoreRangeEnum.LEVEL1_SCORE, currentGroup)).thenReturn(level1List);

        dposAddresses = dposService.calcNextDposNodes(toBeBestBlock, maxBlockHeight);
        Assert.assertTrue(CollectionUtils.isNotEmpty(dposAddresses) && dposAddresses.size() == 7);

        // If the selected miners are not enough, then select others from the left miners.
        level2List = Collections.emptyList();
        PowerMockito.when(scoreService.queryAddresses(ScoreRangeEnum.LEVEL2_SCORE, currentGroup)).thenReturn(level2List);
        dposAddresses = dposService.calcNextDposNodes(toBeBestBlock, maxBlockHeight);
        Assert.assertTrue(CollectionUtils.isNotEmpty(dposAddresses) && dposAddresses.size() == 7);

        //can not find enough dpos node,sn+1
        level5List = getTestAddresses("level_5_List", 2);
        level4List = getTestAddresses("level_4_List", 3);
        level3List = getTestAddresses("level_3_List", 1);
        level1List = Collections.emptyList();
        PowerMockito.when(scoreService.queryAddresses(ScoreRangeEnum.LEVEL5_SCORE, currentGroup)).thenReturn(level5List);
        PowerMockito.when(scoreService.queryAddresses(ScoreRangeEnum.LEVEL4_SCORE, currentGroup)).thenReturn(level4List);
        PowerMockito.when(scoreService.queryAddresses(ScoreRangeEnum.LEVEL3_SCORE, currentGroup)).thenReturn(level3List);
        PowerMockito.when(scoreService.queryAddresses(ScoreRangeEnum.LEVEL1_SCORE, currentGroup)).thenReturn(level1List);
        dposAddresses = dposService.calcNextDposNodes(toBeBestBlock, maxBlockHeight);
        Assert.assertTrue(CollectionUtils.isNotEmpty(dposAddresses) && dposAddresses.size() == 6);
    }

    @Test
    public void getDposGroupBySn() {
        PowerMockito.when(dposRepository.findBySn(SN)).thenReturn(null);
        Assert.assertTrue(CollectionUtils.isEmpty(dposService.getDposGroupBySn(SN)));

        PowerMockito.when(dposRepository.findBySn(SN)).thenReturn(getDposEntity());
        Assert.assertEquals(getTestAddresses("address", 3), dposService.getDposGroupBySn(SN));
    }

    @Test
    public void getRestDposMinersByPreHash() {
        //preBlockHash is null
        String preBlockHash = null;
        Assert.assertTrue(CollectionUtils.isEmpty(dposService.getRestDposMinersByPreHash(preBlockHash)));

        //preBlockHash is not null but preBlock is null
        preBlockHash = "preBlockHash";
        PowerMockito.when(blockService.getBlockByHash(preBlockHash)).thenReturn(null);
        Assert.assertTrue(CollectionUtils.isEmpty(dposService.getRestDposMinersByPreHash(preBlockHash)));

        //preBlock is not null
        Block preBlock = new Block();
        long preBlockHeight = 7;
        //This height corresponding sn has no dpos node
        preBlock.setHeight(preBlockHeight);
        preBlock.setHash(preBlockHash);
        preBlock.setPrevBlockHash("previous Block Hash");
        PowerMockito.when(blockService.getBlockByHash(preBlockHash)).thenReturn(preBlock);
        Assert.assertTrue(CollectionUtils.isEmpty(dposService.getRestDposMinersByPreHash(preBlockHash)));

        //mock dpos by sn
        List<String> dposGroupBySn = getTestAddresses("dposAddress", 7);
        long sn = spyDposService.calculateSn(preBlockHeight + 1);
        Mockito.doReturn(dposGroupBySn).when(spyDposService).getDposGroupBySn(sn);
        Assert.assertTrue(CollectionUtils.isNotEmpty(spyDposService.getRestDposMinersByPreHash(preBlockHash)));
    }

    @Test
    public void checkProducer() {
        String address = "address_5";
        SignaturePair minerPKSig = PowerMockito.spy(new SignaturePair());
        PowerMockito.when(minerPKSig.getAddress()).thenReturn(address);

        Block preBlock = new Block();
        preBlock.setPrevBlockHash("preBlockHash");
        preBlock.setMinerSigPair(minerPKSig);
        List<String> dposGroupBySn = getTestAddresses("address", 7);
        Mockito.doReturn(dposGroupBySn).when(spyDposService).getRestDposMinersByPreHash(preBlock.getPrevBlockHash());
        //the dposGroupBySn contains address
        Assert.assertTrue(spyDposService.checkProducer(preBlock));

        //the dposGroupBySn do not contains address
        address = "address";
        PowerMockito.when(minerPKSig.getAddress()).thenReturn(address);
        Assert.assertFalse(spyDposService.checkProducer(preBlock));

        //dposGroupBySn is null
        Mockito.doReturn(null).when(spyDposService).getRestDposMinersByPreHash(preBlock.getPrevBlockHash());
        Assert.assertFalse(spyDposService.checkProducer(preBlock));

        //dposGroupBySn is empty List
        Mockito.doReturn(Collections.emptyList()).when(spyDposService).getRestDposMinersByPreHash(preBlock.getPrevBlockHash());
        Assert.assertFalse(spyDposService.checkProducer(preBlock));
    }

    @Test
    public void canPackBlock() {
        String preBlockHash = "preBlockHash";
        long height = 10L;
        String address = "address";
        Mockito.doReturn(null).when(spyDposService).getRestDposMinersByPreHash(preBlockHash);
        Assert.assertFalse(spyDposService.canPackBlock(height, address, preBlockHash));

        //the dposGroupBySn do not contains address
        List<String> dposGroupBySn = getTestAddresses("address", 7);
        Mockito.doReturn(dposGroupBySn).when(spyDposService).getRestDposMinersByPreHash(preBlockHash);
        Assert.assertFalse(spyDposService.canPackBlock(height, address, preBlockHash));

        //the dposGroupBySn  contains address
        address = "address_4";
        Assert.assertTrue(spyDposService.canPackBlock(height, address, preBlockHash));

    }

    @Test
    public void calculateStartHeight() {

        Assert.assertEquals(1L, dposService.calculateStartHeight(1L));

        long height = 7L;

        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(7L, dposService.calculateStartHeight(height));
            height++;
        }
    }

    @Test
    public void calculateEndHeight() {

        Assert.assertEquals(1L, dposService.calculateEndHeight(1L));

        long startHeight = 7L;
        long endHeight = 11L;
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(endHeight, dposService.calculateEndHeight(startHeight));
            startHeight++;
        }

        startHeight = 12L;
        Assert.assertEquals(16L, dposService.calculateEndHeight(startHeight));
    }

    @Test
    public void calculateSn() {
        long height = 1L;
        Assert.assertEquals(1L, dposService.calculateSn(height));

        height = 2L;
        Assert.assertEquals(2L, dposService.calculateSn(height));

        height = 7L;
        Assert.assertEquals(3L, dposService.calculateSn(height));

        height = 10L;
        Assert.assertEquals(3L, dposService.calculateSn(height));
    }

    @Test
    public void checkBlockUnstrictly() {
        //block is null
        Assert.assertFalse(dposService.checkBlockUnstrictly(null));

        //getDposGroupBySn result is empty
        Block block = new Block();
        block.setHeight(10L);
        Assert.assertFalse(dposService.checkBlockUnstrictly(block));

        List<String> miners = getTestAddresses("miner", 7);
        Mockito.doReturn(miners).when(spyDposService).getDposGroupBySn(3);

        String address = "address_5";
        SignaturePair minerPKSig = PowerMockito.spy(new SignaturePair());
        block.setMinerSigPair(minerPKSig);
        PowerMockito.when(minerPKSig.getAddress()).thenReturn(address);
        //the address is not dpos
        Assert.assertFalse(spyDposService.checkBlockUnstrictly(block));

        address = "miner_4";
        PowerMockito.when(minerPKSig.getAddress()).thenReturn(address);
        Assert.assertTrue(spyDposService.checkBlockUnstrictly(block));
    }

    public static List<String> getTestAddresses(String baseAddress, final int num) {
        List<String> addresses = new ArrayList<>(num);
        int addressLength = baseAddress.length();
        StrBuilder builder = new StrBuilder(baseAddress);
        for (int i = 0; i < num; i++) {
            builder.append("_").append(i);
            addresses.add(builder.toString());
            builder.delete(addressLength, builder.length());
        }
        return addresses;
    }

    private DposEntity getDposEntity() {
        List<String> addresses = getTestAddresses("address", 3);
        DposEntity dposEntity = new DposEntity();
        dposEntity.setSn(SN);
        dposEntity.setAddresses(JSONObject.toJSONString(addresses));
        return dposEntity;
    }
}