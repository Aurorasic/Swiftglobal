package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.dao.ITransactionIndexRepository;
import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.Transactional;
import com.higgsblock.global.chain.app.service.IBalanceService;
import com.higgsblock.global.chain.app.service.ITransactionIndexService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The TransactionService includes transaction index and utxo persistence.
 *
 * @author Zhao xiaogang
 * @date 2018 -05-22
 */
@Service
@Slf4j
public class TransactionIndexService implements ITransactionIndexService {
    @Autowired
    private BestUTXOService bestUtxoService;

    @Autowired
    private UTXOServiceProxy utxoServiceProxy;

    /**
     * The Transaction index entity dao.
     */
    @Autowired
    private ITransactionIndexRepository transactionIndexRepository;

    @Autowired
    private IBalanceService balanceService;

    @Override
    public TransactionIndexEntity findByTransactionHash(String txHash) {
        return transactionIndexRepository.findByTransactionHash(txHash);
    }

    @Override
    public void addTxIndexAndUtxo(Block toBeBestBlock, String bestBlockHash) {

        List<Transaction> transactionList = toBeBestBlock.getTransactions();
        final int txSize = transactionList.size();
        for (int txCount = 0; txCount < txSize; txCount++) {
            Transaction tx = transactionList.get(txCount);

            //add new tx index
            saveTxIndex(tx, bestBlockHash, txCount);

            List<TransactionInput> inputList = tx.getInputs();

            if (CollectionUtils.isNotEmpty(inputList)) {
                for (TransactionInput input : inputList) {
                    TransactionOutPoint outPoint = input.getPrevOut();
                    String spentTxHash = outPoint.getTransactionHash();
                    short spentTxOutIndex = outPoint.getIndex();
                    //TODO remove validate tangKun 2018-10-11
//                    TransactionIndex txIndex = getTransactionIndex(spentTxHash);
//                    if (txIndex == null) {
//                        throw new IllegalStateException("Spent tx not exits: " + spentTxHash + toBeBestBlock.getSimpleInfoSuffix());
//                    }
                    //remove spent utxo
                    String utxoKey = UTXO.buildKey(spentTxHash, spentTxOutIndex);
                    if (utxoServiceProxy.getUTXOOnBestChain(utxoKey) == null) {
                        throw new IllegalStateException("UTXO not exists : " + utxoKey + toBeBestBlock.getSimpleInfoSuffix());
                    }
                    bestUtxoService.deleteByTransactionHashAndOutIndex(spentTxHash, spentTxOutIndex);
                    balanceService.minusBalance(new UTXO(spentTxHash, spentTxOutIndex, outPoint.getOutput()));
                }
            }

            //add new utxo
            List<TransactionOutput> outputs = tx.getOutputs();
            if (CollectionUtils.isNotEmpty(outputs)) {
                final int outputSize = outputs.size();
                for (int i = 0; i < outputSize; i++) {
                    TransactionOutput output = outputs.get(i);
                    UTXO utxo = new UTXO(tx, (short) i, output);
                    bestUtxoService.saveUTXO(utxo);
                    balanceService.plusBalance(utxo);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<Transaction> getTxOfUnSpentUtxo(String preBlockHash, List<Transaction> cacheTransactions) {
        List<Transaction> result = new LinkedList<>();

        if (CollectionUtils.isEmpty(cacheTransactions)) {
            return result;
        }

        HashMap<String, String> spentUTXOMap = new HashMap<>(8);
        int size = cacheTransactions.size();
        for (int i = size - 1; i >= 0; i--) {
            Transaction tx = cacheTransactions.get(i);
            List<TransactionInput> inputs = tx.getInputs();
            if (CollectionUtils.isEmpty(inputs)) {
                continue;
            }
            boolean unspentUtxoTx = true;
            for (TransactionInput input : inputs) {
                String preUTXOKey = input.getPreUTXOKey();
                if (spentUTXOMap.containsKey(preUTXOKey)) {
                    unspentUtxoTx = false;
                    LOGGER.warn("there has two or one tx try to spent same uxto={}," +
                                    "old spend tx={}, other spend tx={}", preUTXOKey,
                            spentUTXOMap.get(preUTXOKey), tx.getHash());
                    break;
                }

                UTXO utxo = utxoServiceProxy.getUnionUTXO(preBlockHash, preUTXOKey);
                if (utxo == null) {
                    unspentUtxoTx = false;
                    LOGGER.warn("utxo data map has no this uxto={},tx={}", preUTXOKey, tx.getHash());
                    break;
                }

                spentUTXOMap.put(preUTXOKey, tx.getHash());
            }
            if (unspentUtxoTx) {
                result.add(tx);
            }
        }
        return result;
    }

    /**
     * Save tx index.
     *
     * @param tx            the tx
     * @param bestBlockHash the best block hash
     * @param txCount       the tx count
     */
    private void saveTxIndex(Transaction tx, String bestBlockHash, int txCount) {
        TransactionIndex newTxIndex = new TransactionIndex(bestBlockHash, tx.getHash(), (short) txCount);

        TransactionIndexEntity transactionIndexEntity = new TransactionIndexEntity();
        transactionIndexEntity.setBlockHash(newTxIndex.getBlockHash());
        transactionIndexEntity.setTransactionHash(newTxIndex.getTxHash());
        transactionIndexEntity.setTransactionIndex(newTxIndex.getTxIndex());

        transactionIndexRepository.save(transactionIndexEntity);
    }

    /**
     * Gets transaction index.
     *
     * @param spentTxHash the spent tx hash
     * @return the transaction index
     */
    private TransactionIndex getTransactionIndex(String spentTxHash) {
        TransactionIndexEntity entity = transactionIndexRepository.findByTransactionHash(spentTxHash);
        if (entity == null) {
            return null;
        }

        TransactionIndex transactionIndex = new TransactionIndex();
        transactionIndex.setBlockHash(entity.getBlockHash());
        transactionIndex.setTxHash(entity.getTransactionHash());
        transactionIndex.setTxIndex(entity.getTransactionIndex());

        return transactionIndex;
    }

}
