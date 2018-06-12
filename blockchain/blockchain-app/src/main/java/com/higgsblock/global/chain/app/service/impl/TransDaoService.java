package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.dao.BlockDao;
import com.higgsblock.global.chain.app.dao.TransDao;
import com.higgsblock.global.chain.app.dao.UtxoDao;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import com.higgsblock.global.chain.app.service.ITransService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Zhao xiaogang
 * @date 2018-05-22
 */
@Service
@Slf4j
public class TransDaoService implements ITransService {

    @Autowired
    private TransDao transDao;

    @Autowired
    private TransactionCacheManager txCacheManager;

    @Autowired
    private UtxoDao utxoDao;

    @Autowired
    private BlockDao blockDao;

    @Override
    public List<BaseDaoEntity> addTransIdxAndUtxo(Block bestBlock, String bestBlockHash) throws Exception {
        List<BaseDaoEntity> entityList = new ArrayList<>();
        List<Transaction> transactionList = bestBlock.getTransactions();
        final int txSize = transactionList.size();
        for (int txCount = 0; txCount < txSize; txCount++) {
            Transaction tx = transactionList.get(txCount);

            //add new tx index
            TransactionIndex newTxIndex = new TransactionIndex(bestBlockHash, tx.getHash(), (short) txCount);

            BaseDaoEntity baseDaoEntity = transDao.getEntity(tx.getHash(), newTxIndex);
            entityList.add(baseDaoEntity);

            List<TransactionInput> inputList = tx.getInputs();

            if (CollectionUtils.isNotEmpty(inputList)) {
                for (TransactionInput input : inputList) {
                    TransactionOutPoint outPoint = input.getPrevOut();
                    String spentTxHash = outPoint.getHash();
                    short spentTxOutIndex = outPoint.getIndex();

                    TransactionIndex txIndex = transDao.get(spentTxHash);
                    if (txIndex == null) {
                        throw new IllegalStateException("Spent tx not exits: " + spentTxHash);
                    }

                    txIndex.addSpend(spentTxOutIndex, tx.getHash());

                    //update the pre-transaction state
                    BaseDaoEntity entity = transDao.getEntity(txIndex.getTxHash(), txIndex);
                    entityList.add(entity);

                    //remove spent utxo
                    String utxoKey = UTXO.buildKey(spentTxHash, spentTxOutIndex);
                    if (utxoDao.get(utxoKey) == null) {
                        throw new IllegalStateException("UTXO not exists : " + utxoKey);
                    }

                    BaseDaoEntity utxoEntity = utxoDao.getEntity(utxoKey, null);
                    entityList.add(utxoEntity);
                }
            }

            //add new utxo
            List<TransactionOutput> outputs = tx.getOutputs();
            if (CollectionUtils.isNotEmpty(outputs)) {
                final int outputSize = outputs.size();
                for (int i = 0; i < outputSize; i++) {
                    TransactionOutput output = outputs.get(i);
                    UTXO utxo = new UTXO(tx, (short) i, output);

                    BaseDaoEntity utxoEntity = utxoDao.getEntity(utxo.getKey(), utxo);
                    entityList.add(utxoEntity);
                }
            }
        }

        return entityList;
    }

    @Override
    public void removeDoubleSpendTx(List<Transaction> cacheTransactions) {
        if (CollectionUtils.isEmpty(cacheTransactions)) {
            return;
        }

        HashMap<String, String> spentUTXOMap = new HashMap<>(8);
        int size = cacheTransactions.size();
        for (int i = size - 1; i >= 0; i--) {
            Transaction tx = cacheTransactions.get(i);
            List<TransactionInput> inputs = tx.getInputs();
            if (CollectionUtils.isEmpty(inputs)) {
                continue;
            }
            for (TransactionInput input : inputs) {
                String preUTXOKey = input.getPreUTXOKey();
                if (spentUTXOMap.containsKey(preUTXOKey)) {
                    txCacheManager.remove(tx.getHash());
                    cacheTransactions.remove(i);
                    LOGGER.warn("there has two or one tx try to spent same uxto={}," +
                                    "old spend tx={}, other spend tx={}", preUTXOKey,
                            spentUTXOMap.get(preUTXOKey), tx.getHash());
                    break;
                }

                if (!utxoDao.allKeys().contains(preUTXOKey)) {
                    txCacheManager.remove(tx.getHash());
                    cacheTransactions.remove(i);
                    LOGGER.warn("utxo data map has no this uxto={}_tx={}", preUTXOKey, tx.getHash());
                    break;
                }

                spentUTXOMap.put(preUTXOKey, tx.getHash());
            }
        }
    }
}
