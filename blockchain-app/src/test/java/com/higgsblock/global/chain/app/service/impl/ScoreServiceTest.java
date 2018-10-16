package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.SignaturePair;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
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
import org.mockito.Spy;
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

    @InjectMocks
    @Spy
    private IScoreService spyScoreService = new ScoreService();

    @Mock
    private IBlockChainInfoService blockChainInfoService;

    @Mock
    private ITransactionService transactionService;

    @Mock
    private IDposService dposService;

    @Test
    public void get() {
    }

    @Test
    public void put() {
    }

    @Test
    public void put1() {
    }

    @Test
    public void updateBatch() {
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
        //value is not null
        String minerAddress = "minerAddress1";
        scoreService.remove(minerAddress);
        Assert.assertTrue(scoreMap.size() == 2);

        //value is null
        minerAddress = "address";
        scoreService.remove(minerAddress);
        Assert.assertTrue(scoreMap.size() == 3);
    }

    @Test
    public void setSelectedDposScore() {
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
        for (int i = 0; i < size; i++) {
            allScores.put(address + i, String.valueOf(baseScore));
        }
        return allScores;
    }
}