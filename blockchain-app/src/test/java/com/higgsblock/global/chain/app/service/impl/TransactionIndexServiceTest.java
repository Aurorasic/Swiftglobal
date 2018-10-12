package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.ITransactionIndexRepository;
import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;
import com.higgsblock.global.chain.app.service.IBalanceService;
import com.higgsblock.global.chain.app.service.ITransactionIndexService;
import com.higgsblock.global.chain.app.utils.GetTransactionTestObj;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;

/**
 * @author Su Jiulong
 * @date 2018/10/10
 */
public class TransactionIndexServiceTest extends BaseMockTest {
    @InjectMocks
    private ITransactionIndexService transactionIndexService = new TransactionIndexService();

    @Mock
    private ITransactionIndexRepository transactionIndexRepository;

    @Mock
    private UTXOServiceProxy utxoServiceProxy;

    @Mock
    private BestUTXOService bestUtxoService;

    @Mock
    private IBalanceService balanceService;

    @Test
    public void findByTransactionHash() {
        TransactionIndexEntity txIndexEntity = new TransactionIndexEntity();
        String txHash = "transactionHash";
        PowerMockito.when(transactionIndexRepository.findByTransactionHash(txHash)).thenReturn(txIndexEntity);
        Assert.assertEquals(txIndexEntity, transactionIndexService.findByTransactionHash(txHash));
    }

    @Test
    public void addTxIndexAndUtxo() throws Exception {
        Block bestBlock = new Block();
        String bestBlockHash = "bestBlockHash";
        bestBlock.setHash(bestBlockHash);
        Transaction transaction = GetTransactionTestObj.getSingleTransaction();
        List<Transaction> transactions = new ArrayList<>(1);
        transactions.add(transaction);
        bestBlock.setTransactions(transactions);
        //transaction index is null
        PowerMockito.when(transactionIndexRepository.save(any(TransactionIndexEntity.class))).thenReturn(new TransactionIndexEntity());
        try {
            transactionIndexService.addTxIndexAndUtxo(bestBlock, bestBlockHash);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("Spent tx not exits:"));
        }

        //UTXO not exists
        TransactionIndexEntity txIndexEntity = new TransactionIndexEntity();
        PowerMockito.when(transactionIndexRepository.findByTransactionHash(anyString())).thenReturn(txIndexEntity);
        PowerMockito.when(utxoServiceProxy.getUTXOOnBestChain(anyString())).thenReturn(null);
        try {
            transactionIndexService.addTxIndexAndUtxo(bestBlock, bestBlockHash);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalStateException);
            Assert.assertTrue(e.getMessage().contains("UTXO not exists"));
        }
        //UTXO exists
        PowerMockito.when(utxoServiceProxy.getUTXOOnBestChain(anyString())).thenReturn(new UTXO());
        PowerMockito.doNothing().when(bestUtxoService).deleteByTransactionHashAndOutIndex(anyString(), anyShort());
        PowerMockito.doNothing().when(balanceService).minusBalance(any(UTXO.class));
        PowerMockito.doNothing().when(bestUtxoService).saveUTXO(any(UTXO.class));
        PowerMockito.doNothing().when(balanceService).plusBalance(any(UTXO.class));
        transactionIndexService.addTxIndexAndUtxo(bestBlock, bestBlockHash);
    }

    @Test
    public void getTxOfUnSpentUtxo() {
        String preBlockHash = "preBlockHash";
        //cache transactions is empty
        List<Transaction> cacheTransactions = new ArrayList<>(1);
        List<Transaction> result = transactionIndexService.getTxOfUnSpentUtxo(preBlockHash, cacheTransactions);
        Assert.assertTrue(CollectionUtils.isEmpty(result));

        //cache transactions is not empty
        cacheTransactions.add(GetTransactionTestObj.getSingleTransaction());
        //utxo is null
        PowerMockito.when(utxoServiceProxy.getUnionUTXO(anyString(), anyString())).thenReturn(null);
        result = transactionIndexService.getTxOfUnSpentUtxo(preBlockHash, cacheTransactions);
        Assert.assertTrue(CollectionUtils.isEmpty(result));

        //the transaction inputs is null
        cacheTransactions.add(new Transaction());
        cacheTransactions.add(GetTransactionTestObj.getSingleTransaction());
        final UTXO utxo = new UTXO();
        PowerMockito.when(utxoServiceProxy.getUnionUTXO(anyString(), anyString())).thenReturn(utxo);
        result = transactionIndexService.getTxOfUnSpentUtxo(preBlockHash, cacheTransactions);
        Assert.assertTrue(CollectionUtils.isNotEmpty(result) && (result.size() == 1));
    }
}