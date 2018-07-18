//package com.higgsblock.global.chain.app.service.impl;
//
//import com.google.common.collect.Lists;
//import com.higgsblock.global.chain.app.BaseMockTest;
//import com.higgsblock.global.chain.app.blockchain.Block;
//import com.higgsblock.global.chain.app.blockchain.transaction.*;
//import com.higgsblock.global.chain.app.dao.TransDao;
//import com.higgsblock.global.chain.app.dao.UtxoDao;
//import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
//import com.higgsblock.global.chain.app.blockchain.script.LockScript;
//import com.higgsblock.global.chain.common.utils.Money;
//import org.junit.Assert;
//import org.junit.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.powermock.api.mockito.PowerMockito;
//
//import static org.mockito.ArgumentMatchers.anyString;
//
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.rocksdb.RocksDBException;
//
//import java.util.List;
//
///**
// * @author Su Jiulong
// * @date 2018-06-26
// */
//@PrepareForTest({TransDaoService.class})
//public class TransDaoServiceTest extends BaseMockTest {
//
//    @Mock
//    private TransDao transDao;
//    @Mock
//    private UtxoDao utxoDao;
//    @Mock
//    private TransactionCacheManager txCacheManager;
//    @InjectMocks
//    private TransDaoService transDaoService;
//
//    @Test
//    public void addTransIdxAndUtxo() throws Exception {
//        Block bestBlock = new Block();
//        bestBlock.setHash("bestBlockHash");
//
//        List<Transaction> transactions = Lists.newArrayList();
//        Transaction transaction = new Transaction();
//        String transactionHash = "transactionHash";
//        transaction.setHash(transactionHash);
//        //transaction inputs
//        List<TransactionInput> txInputs = Lists.newArrayList();
//        TransactionInput txInput = new TransactionInput();
//        TransactionOutPoint txOutPoint = new TransactionOutPoint();
//        String spentTxHash = "preTxHash";
//        txOutPoint.setHash(spentTxHash);
//        short spentTxOutIndex = 1;
//
//        txOutPoint.setIndex(spentTxOutIndex);
//        txInput.setPrevOut(txOutPoint);
//        txInputs.add(txInput);
//        transaction.setInputs(txInputs);
//
//        //transaction outputs
//        List<TransactionOutput> txOutputs = Lists.newArrayList();
//        TransactionOutput txOutput = new TransactionOutput();
//        Money money = new Money(1);
//        LockScript lockScript = new LockScript();
//        lockScript.setAddress("address");
//        lockScript.setType(ScriptTypeEnum.P2PKH.getType());
//        txOutput.setMoney(money);
//        txOutput.setLockScript(lockScript);
//        txOutputs.add(txOutput);
//        transaction.setOutputs(txOutputs);
//
//        transactions.add(transaction);
//        bestBlock.setTransactions(transactions);
//        //partial mock TransactionIndex
//        TransactionIndex newTxIndex = PowerMockito.spy(new TransactionIndex());
//        newTxIndex.setBlockHash(bestBlock.getHash());
//        newTxIndex.setTxHash(transactionHash);
//        newTxIndex.setTxIndex((short) 0);
//        PowerMockito.whenNew(TransactionIndex.class).withAnyArguments().thenReturn(newTxIndex);
//
//        BaseDaoEntity baseDaoEntity = PowerMockito.mock(BaseDaoEntity.class);
//        PowerMockito.when(transDao.getEntity(transactionHash, newTxIndex)).thenReturn(baseDaoEntity);
//        //Spent tx not exits:
//        PowerMockito.when(transDao.get(spentTxHash)).thenReturn(null);
//        try {
//            transDaoService.addTransIdxAndUtxo(bestBlock, bestBlock.getHash());
//        } catch (Exception e) {
//            Assert.assertTrue(e instanceof IllegalStateException);
//            Assert.assertEquals("Spent tx not exits: " + spentTxHash, e.getMessage());
//        }
//
//        TransactionIndex txIndex = new TransactionIndex();
//        txIndex.setTxHash(spentTxHash);
//        BaseDaoEntity updateDaoEntity = PowerMockito.mock(BaseDaoEntity.class);
//        PowerMockito.when(transDao.get(anyString())).thenReturn(txIndex);
//        PowerMockito.when(transDao.getEntity(txIndex.getTxHash(), txIndex)).thenReturn(updateDaoEntity);
//        //UTXO not exists
//        PowerMockito.when(utxoDao.get(anyString())).thenReturn(null);
//        try {
//            transDaoService.addTransIdxAndUtxo(bestBlock, bestBlock.getHash());
//        } catch (Exception e) {
//            Assert.assertTrue(e instanceof IllegalStateException);
//            Assert.assertTrue(e.getMessage().contains("UTXO not exists"));
//        }
//
//        //UTXO exists
//        UTXO utxo = new UTXO();
//        PowerMockito.when(utxoDao.get(anyString())).thenReturn(utxo);
//        BaseDaoEntity utxoEntity = PowerMockito.mock(BaseDaoEntity.class);
//        String utxoKey = transaction.getInputs().get(0).getPreUTXOKey();
//        PowerMockito.when(utxoDao.getEntity(utxoKey, null)).thenReturn(utxoEntity);
//
//        //add new UTXO
//        UTXO newUTXO = new UTXO(transaction, (short) 0, transaction.getOutputs().get(0));
//        PowerMockito.whenNew(UTXO.class).withAnyArguments().thenReturn(newUTXO);
//        List<BaseDaoEntity> baseDaoEntities = transDaoService.addTransIdxAndUtxo(bestBlock, bestBlock.getHash());
//        Assert.assertEquals(4, baseDaoEntities.size());
//    }
//
//    @Test
//    public void getTxOfUnSpentUtxo() throws RocksDBException {
//        //transactions is empty
//        List<Transaction> cacheTransactions = Lists.newArrayList();
//        transDaoService.getTxOfUnSpentUtxo(cacheTransactions);
//        //transactions is not empty
//        for (int i = 0; i < 5; i++) {
//            Transaction transaction = new Transaction();
//            String transactionHash = "transactionHash" + i;
//            transaction.setHash(transactionHash);
//            //transaction inputs
//            if (i != 4) {
//                List<TransactionInput> txInputs = Lists.newArrayList();
//                TransactionInput txInput = new TransactionInput();
//                TransactionOutPoint txOutPoint = new TransactionOutPoint();
//                String spentTxHash = "preTxHash";
//                txOutPoint.setHash(spentTxHash);
//                short spentTxOutIndex = 1;
//                if (i == 0) {
//                    spentTxOutIndex = 0;
//                }
//                txOutPoint.setIndex(spentTxOutIndex);
//                txInput.setPrevOut(txOutPoint);
//                txInputs.add(txInput);
//                transaction.setInputs(txInputs);
//            }
//
//            //transaction outputs
//            List<TransactionOutput> txOutputs = Lists.newArrayList();
//            TransactionOutput txOutput = new TransactionOutput();
//            Money money = new Money(1);
//            LockScript lockScript = new LockScript();
//            lockScript.setAddress("address");
//            lockScript.setType(ScriptTypeEnum.P2PKH.getType());
//            txOutput.setMoney(money);
//            txOutput.setLockScript(lockScript);
//            txOutputs.add(txOutput);
//            transaction.setOutputs(txOutputs);
//            cacheTransactions.add(transaction);
//        }
//        UTXO utxo = new UTXO();
//        PowerMockito.when(utxoDao.get(anyString()))
//                //remove double Spending transaction
//                .thenReturn(null)
//                //remove repeat transaction
//                .thenReturn(utxo)
//                .thenThrow(new RocksDBException("get utxo error"));
//        PowerMockito.doNothing().when(txCacheManager).remove(anyString());
//        transDaoService.getTxOfUnSpentUtxo(cacheTransactions);
//
//    }
//}