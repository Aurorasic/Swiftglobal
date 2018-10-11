package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.BaseMockTest;
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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yangshenghong
 * @date 2018-10-11
 */
@PrepareForTest
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
        Mockito.doReturn(map).when(spyBalanceService).get(address);
        Assert.assertEquals(spyBalanceService.getBalanceOnBest(address, currency).getCurrency(), currency);

        //The result of the query contains the specified currency
        Money money = new Money(100, "CAS");
        map.put("currency", money);
        Assert.assertEquals(spyBalanceService.getBalanceOnBest(address, currency), money);
    }

    @Test
    public void get() {
        String address = "address";
        //find balance by address return null
        PowerMockito.when(balanceRepository.findOne(address)).thenReturn(null);
        Assert.assertTrue(CollectionUtils.isEmpty(balanceService.get(address)));

        //find balance by address return balanceEntity but balances list is empty
        BalanceEntity balanceEntity = new BalanceEntity();
        PowerMockito.when(balanceRepository.findOne(address)).thenReturn(balanceEntity);
        Assert.assertTrue(CollectionUtils.isEmpty(balanceService.get(address)));

        //query to the result and complete the processing return
        balanceEntity.setBalances(Arrays.asList(new Money(0, "cas"), new Money(0, "miner")));
        Assert.assertTrue(balanceService.get(address).containsKey("cas"));
    }

    @Test
    public void plusBalance() {
        //the money in the result of single query is not empty
        UTXO utxo = new UTXO();
        TransactionOutput output = new TransactionOutput();
        output.setMoney(new Money(3, "cas"));
        utxo.setOutput(output);
        Map<String, Money> balanceMap = new HashMap<>();
        balanceMap.put("cas", new Money(1, "cas"));
        balanceMap.put("miner", new Money(2, "miner"));
        Mockito.doReturn(balanceMap).when(spyBalanceService).get(utxo.getAddress());
        spyBalanceService.plusBalance(utxo);

        //the money in the result of single query is not null
        balanceMap.put("cas", null);
        spyBalanceService.plusBalance(utxo);
    }

    @Test
    public void minusBalance() {
        //when get balance by address return null or empty
        UTXO utxo = new UTXO();
        Mockito.doReturn(null).when(spyBalanceService).get(utxo.getAddress());
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
        Mockito.doReturn(balanceMap).when(spyBalanceService).get(utxo.getAddress());
        try {
            spyBalanceService.minusBalance(utxo);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeExceptionProxy);
            Assert.assertTrue(e.getMessage().contains("can't find currency balance"));
        }

        //the result of the query is not null and contains currency
        output.setMoney(new Money(3, "cas"));
        spyBalanceService.minusBalance(utxo);
    }

    @Test
    public void getUnionBalance() throws Exception {

    }

}