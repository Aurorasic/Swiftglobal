package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.IBalanceRepository;
import com.higgsblock.global.chain.app.dao.entity.BalanceEntity;
import com.higgsblock.global.chain.app.service.IBlockIndexService;
import com.higgsblock.global.chain.common.utils.Money;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.mockito.internal.mockcreation.RuntimeExceptionProxy;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author yangshenghong
 * @date 2018-10-11
 */
public class BalanceServiceTest extends BaseMockTest {
    @InjectMocks
    private BalanceService balanceService;

    @InjectMocks
    @Spy
    private BalanceService spyBalanceService = new BalanceService();

    @Mock
    private IBalanceRepository balanceRepository;
    @Mock
    private UTXOServiceProxy utxoServiceProxy;
    @Mock
    private IBlockIndexService blockIndexService;

    @Test
    public void getBalanceOnBest() {
        String address = "address",
                currency = "currency";
        //There is no specified currency in the result of the query
        Map<String, Money> map = Maps.newHashMap();
        Mockito.doReturn(map).when(spyBalanceService).getBalanceByAddress(address);
        Assert.assertEquals(spyBalanceService.getBalanceOnBest(address, currency).getCurrency(), currency);

        //The result of the query contains the specified currency
        Money money = new Money(100, "CAS");
        map.put("currency", money);
        Assert.assertEquals(spyBalanceService.getBalanceOnBest(address, currency), money);
    }

    @Test
    public void getBalanceByAddress() {
        String address = "address";
        //find balance by address return null
        PowerMockito.when(balanceRepository.findOne(address)).thenReturn(null);
        Assert.assertTrue(CollectionUtils.isEmpty(balanceService.getBalanceByAddress(address)));

        //find balance by address return balanceEntity but balances list is empty
        BalanceEntity balanceEntity = new BalanceEntity();
        PowerMockito.when(balanceRepository.findOne(address)).thenReturn(balanceEntity);
        Assert.assertTrue(CollectionUtils.isEmpty(balanceService.getBalanceByAddress(address)));

        //query to the result and complete the processing return
        balanceEntity.setBalances(Arrays.asList(new Money(0, "cas"), new Money(0, "miner")));
        Assert.assertTrue(balanceService.getBalanceByAddress(address).containsKey("cas"));
    }

    @Test
    public void plusBalance() {
        //the money in the result of single query is empty
        UTXO utxo = new UTXO();
        TransactionOutput output = new TransactionOutput();
        output.setMoney(new Money(3, "miner"));
        utxo.setOutput(output);
        Map<String, Money> balanceMap = new HashMap<>();
        Mockito.doReturn(balanceMap).when(spyBalanceService).getBalanceByAddress(utxo.getAddress());
        spyBalanceService.plusBalance(utxo);

        //the money in the result of single query is not empty and the specified currency is not included in the map
        balanceMap.put("cas", new Money(2, "cas"));
        spyBalanceService.plusBalance(utxo);

        //the money in the result of single query is not empty
        balanceMap.put("miner", new Money(1, "miner"));
        Mockito.doReturn(balanceMap).when(spyBalanceService).getBalanceByAddress(utxo.getAddress());
        spyBalanceService.plusBalance(utxo);

        //the money in the result of single query is null
        balanceMap.put("miner", null);
        spyBalanceService.plusBalance(utxo);
    }

    @Test
    public void minusBalance() {
        //when get balance by address return null or empty
        UTXO utxo = new UTXO();
        Mockito.doReturn(null).when(spyBalanceService).getBalanceByAddress(utxo.getAddress());
        try {
            balanceService.minusBalance(utxo);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("can't find address balance"));
        }

        //the result of the query does not contain the specified currency
        TransactionOutput output = new TransactionOutput();
        output.setMoney(new Money(3, "guarder"));
        utxo.setOutput(output);
        Map<String, Money> balanceMap = new HashMap<>();
        balanceMap.put("cas", new Money(1, "cas"));
        balanceMap.put("miner", new Money(2, "miner"));
        Mockito.doReturn(balanceMap).when(spyBalanceService).getBalanceByAddress(utxo.getAddress());
        try {
            spyBalanceService.minusBalance(utxo);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeExceptionProxy);
            Assert.assertTrue(e.getMessage().contains("can't find currency balance"));
        }

        //the result of the query is null
        balanceMap.put("guarder", null);
        try {
            spyBalanceService.minusBalance(utxo);
        }catch (Exception e){
            Assert.assertTrue(e instanceof  RuntimeException);
            Assert.assertTrue(e.getMessage().contains("error balance address"));
        }

        //the result of the query is not null and contains currency
        output.setMoney(new Money(3, "cas"));
        spyBalanceService.minusBalance(utxo);
    }

    @Test
    public void getUnionBalance() throws Exception {
        String preBlockHash = null,
                address = "address",
                currency = "currency";
        //when preBlockHash empty and lastBlockIndex firstBlockHash is empty
        BlockIndex lastBlockIndex = new BlockIndex();
        PowerMockito.when(blockIndexService.getLastBlockIndex()).thenReturn(lastBlockIndex);
        try {
            spyBalanceService.getUnionBalance(preBlockHash, address, currency);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
            Assert.assertTrue(e.getMessage().contains("error lastBlockIndex"));
        }

        //when preBlockHash empty and lastBlockIndex firstBlockHash is not empty
        lastBlockIndex.setBlockHashs(new ArrayList<>(Arrays.asList("firstBlockHash1", "firstBlockHash2")));
        Money balanceMoney = new Money(0, currency),
                unconfirmedBalanceMoney = new Money(0, currency);
        Mockito.doReturn(balanceMoney).when(spyBalanceService).getBalanceOnBest(address, currency);
        PowerMockito.when(utxoServiceProxy.getUnconfirmedBalance(anyString(), anyString(), anyString())).thenReturn(unconfirmedBalanceMoney);
        Assert.assertEquals(spyBalanceService.getUnionBalance(preBlockHash, address, currency).getCurrency(), currency);

        //when preBlockHash not empty and unconfirmedBalanceMoney not contain currency
        preBlockHash = "preBlockHash";
        unconfirmedBalanceMoney.setCurrency("currency2");
        Assert.assertEquals(spyBalanceService.getUnionBalance(preBlockHash, address, currency).getCurrency(), currency);

        //when preBlockHash not empty
        Assert.assertEquals(spyBalanceService.getUnionBalance(preBlockHash, address, currency).getCurrency(), currency);
    }
}