package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.SignaturePair;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.common.ScoreRangeEnum;
import com.higgsblock.global.chain.app.service.IBlockChainInfoService;
import com.higgsblock.global.chain.app.service.IDposService;
import com.higgsblock.global.chain.app.service.IScoreService;
import com.higgsblock.global.chain.app.service.ITransactionService;
import com.higgsblock.global.chain.app.utils.GetTransactionTestObj;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import org.apache.commons.collections.MapUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Su Jiulong
 * @date 2018/10/15
 */
public class ScoreServiceTest extends BaseMockTest {

    @InjectMocks
    private IScoreService scoreService = new ScoreService();

    @Mock
    private IBlockChainInfoService blockChainInfoService;

    @Mock
    private ITransactionService transactionService;

    @Mock
    private IDposService dposService;

    @Test
    public void get() {
        Map<String, String> scoreMap = getTestScoreMap(3, 800);
        PowerMockito.when(blockChainInfoService.getAllScores()).thenReturn(scoreMap);
        String address = "address";
        Assert.assertTrue(scoreService.get(address) == null);
        address = "minerAddress1";
        Assert.assertEquals("800", scoreService.get(address).toString());
    }

    @Test
    public void put() {
        Map<String, String> scoreMap = getTestScoreMap(3, 800);
        PowerMockito.when(blockChainInfoService.getAllScores()).thenReturn(scoreMap);
        String address = "address";
        Integer score = 1000;
        scoreService.put(address, score);
        Assert.assertTrue(scoreMap.size() == 4);
        Assert.assertTrue(scoreMap.get(address).equals(score.toString()));
    }

    @Test
    public void put1() {
        Map<String, String> scoreMap = getTestScoreMap(3, 800);
        String address = "address";
        Integer score = 1000;
        scoreService.put(address, score, scoreMap);
        Assert.assertTrue(scoreMap.size() == 4);
        Assert.assertTrue(scoreMap.get(address).equals(score.toString()));
    }

    @Test
    public void updateBatch() {
        List<String> addAddressList = DposServiceTest.getTestAddresses("addAddress", 3);
        Map<String, String> scoreMap = getTestScoreMap(3, 800);
        scoreService.updateBatch(addAddressList, 200, scoreMap);
        Assert.assertTrue(scoreMap.size() == 6);
        Assert.assertTrue(scoreMap.get("addAddress_2").equals("200"));
    }

    @Test
    public void plusAll() {
        //allScores is empty
        Map<String, String> allScores = Collections.emptyMap();
        scoreService.plusAll(1, allScores);
        Assert.assertTrue(MapUtils.isEmpty(allScores));

        //allScores is not empty
        Integer score = 800;
        allScores = getTestScoreMap(3, score);
        scoreService.plusAll(1, allScores);
        Assert.assertTrue(allScores.size() == 3);
        final String result = String.valueOf(800 + 1);
        allScores.forEach((k, v) -> {
            Assert.assertTrue(result.equals(v));
        });
    }

    @Test
    public void putIfAbsent() {
        int baseScore = 600;
        int mapSize = 3;
        Map<String, String> scoreMap = getTestScoreMap(mapSize, baseScore);
        PowerMockito.when(blockChainInfoService.getAllScores()).thenReturn(scoreMap);

        //value is not null
        String minerAddress = "minerAddress1";
        int testScore = 800;
        scoreService.putIfAbsent(minerAddress, testScore);
        Assert.assertTrue(scoreMap.size() == 3);
        Assert.assertTrue(scoreMap.get(minerAddress) != null);

        //value is null
        minerAddress = "address";
        scoreService.putIfAbsent(minerAddress, testScore);
        Assert.assertTrue(scoreMap.size() == 4);
        Assert.assertTrue(scoreMap.get(minerAddress).equals(String.valueOf(testScore)));
    }

    @Test
    public void remove() {
        int baseScore = 600;
        Map<String, String> scoreMap = getTestScoreMap(3, baseScore);
        PowerMockito.when(blockChainInfoService.getAllScores()).thenReturn(scoreMap);
        //value is null
        String minerAddress = "address";
        scoreService.remove(minerAddress);
        Assert.assertTrue(scoreMap.size() == 3);

        //value is not null
        minerAddress = "minerAddress1";
        scoreService.remove(minerAddress);
        Assert.assertTrue(scoreMap.size() == 2);
    }

    @Test
    public void setSelectedDposScore() {
        //addAddressList is not empty
        List<String> addAddressList = DposServiceTest.getTestAddresses("addAddress", 3);
        Map<String, String> scoreMap = getTestScoreMap(3, 800);
        PowerMockito.when(blockChainInfoService.getAllScores()).thenReturn(scoreMap);
        scoreService.setSelectedDposScore(addAddressList);
        Assert.assertTrue(scoreMap.size() == 6);
        Assert.assertTrue(scoreMap.get("addAddress_2").equals("600"));

        //addAddressList is empty or null
        addAddressList = null;
        scoreService.setSelectedDposScore(addAddressList);
        addAddressList = Collections.emptyList();
        scoreService.setSelectedDposScore(addAddressList);
    }

    @Test
    public void refreshMinersScore() {
        Block toBeBestBlock = new Block();
        List<Transaction> transactions = new ArrayList<>(1);
        Transaction transaction = GetTransactionTestObj.getSingleTransaction();
        transactions.add(transaction);
        toBeBestBlock.setTransactions(transactions);

        Block newBlock = new Block();
        mockMinerPKSig(toBeBestBlock);
        int baseScore = 600;
        Map<String, String> scoreMap = getTestScoreMap(3, baseScore);
        PowerMockito.when(blockChainInfoService.getAllScores()).thenReturn(scoreMap);
        //if the block is only mined by  miner
        PowerMockito.when(transactionService.
                hasStakeOnBest(toBeBestBlock.getMinerSigPair().getAddress(), SystemCurrencyEnum.MINER)).thenReturn(true);
        scoreService.refreshMinersScore(toBeBestBlock, newBlock);

        //the block mined by guarder
        PowerMockito.when(transactionService.
                hasStakeOnBest(toBeBestBlock.getMinerSigPair().getAddress(), SystemCurrencyEnum.MINER)).thenReturn(false);
        String preBlockHash = "preBlockHash";
        toBeBestBlock.setPrevBlockHash(preBlockHash);
        List<String> dposList = DposServiceTest.getTestAddresses("dposAddress", 5);
        dposList.parallelStream().forEach(dpos -> {
            scoreMap.put(dpos, String.valueOf(baseScore));
        });
        PowerMockito.when(dposService.getRestDposMinersByPreHash(preBlockHash)).thenReturn(dposList);

        Set<String> removeAddress = dposList.subList(0, 2).stream().collect(Collectors.toSet());
        Set<String> addAddress = dposList.subList(2, dposList.size()).stream().collect(Collectors.toSet());
        PowerMockito.when(transactionService.getRemovedMiners(transaction)).thenReturn(removeAddress);
        PowerMockito.when(transactionService.getAddedMiners(transaction)).thenReturn(addAddress);
        scoreService.refreshMinersScore(toBeBestBlock, newBlock);
    }

    @Test
    public void queryAddresses() {
        int baseScore = 900;
        int size = 1099;
        Map<String, String> scoreMap = getTestScoreMap(size, baseScore);
        ScoreRangeEnum level3Score = ScoreRangeEnum.LEVEL3_SCORE;
        int maxScore = level3Score.getMaxScore();
        int minScore = level3Score.getMinScore();
        //[minScore,maxScore)
        String minScoreStr = "minScore";
        String maxScoreStr = "maxScore";
        scoreMap.put(minScoreStr, String.valueOf(minScore));
        scoreMap.put(maxScoreStr, String.valueOf(maxScore));
        PowerMockito.when(blockChainInfoService.getAllScores()).thenReturn(scoreMap);

        Random random = new Random();
        int min = 700;
        int max = 1100;
        int range = max - min;
        List<String> exculdeAddresses = DposServiceTest.getTestAddresses("exculdeAddresses", 99);
        //[700,1200)
        exculdeAddresses.forEach(address -> {
            scoreMap.put(address, String.valueOf(min + random.nextInt(range)));
        });

        Assert.assertEquals(1200, scoreMap.size());
        List<String> list = scoreService.queryAddresses(level3Score, exculdeAddresses);
        Assert.assertEquals(1000, list.size());

        list.parallelStream().forEach(resutl -> {
            Integer value = Integer.valueOf(scoreMap.get(resutl));
            Assert.assertTrue(value >= minScore && value < maxScore);
        });
    }

    private void mockMinerPKSig(Block block) {
        String address = "address";
        SignaturePair minerPKSig = PowerMockito.spy(new SignaturePair());
        block.setMinerSigPair(minerPKSig);
        PowerMockito.when(minerPKSig.getAddress()).thenReturn(address);
    }

    private Map<String, String> getTestScoreMap(int size, int baseScore) {
        Map<String, String> allScores = new HashMap<>(size);
        String address = "minerAddress";
        int baseLength = address.length();
        StringBuilder builder = new StringBuilder(address);
        for (int i = 0; i < size; i++) {
            allScores.put(builder.append(i).toString(), String.valueOf(baseScore));
            builder.delete(baseLength, builder.length());
        }
        return allScores;
    }
}