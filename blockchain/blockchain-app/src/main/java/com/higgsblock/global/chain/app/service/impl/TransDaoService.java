package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.dao.entity.BalanceEntity;
import com.higgsblock.global.chain.app.dao.entity.SpentTransactionOutIndexEntity;
import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import com.higgsblock.global.chain.app.dao.iface.IBalanceEntity;
import com.higgsblock.global.chain.app.dao.impl.SpentTransactionOutIndexEntityDao;
import com.higgsblock.global.chain.app.dao.impl.TransactionIndexEntityDao;
import com.higgsblock.global.chain.app.dao.impl.UTXOEntityDao;
import com.higgsblock.global.chain.app.script.LockScript;
import com.higgsblock.global.chain.app.service.ITransService;
import com.higgsblock.global.chain.common.utils.Money;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Trans dao service.
 *
 * @author Zhao xiaogang
 * @date 2018 -05-22
 */
@Service
@Slf4j
public class TransDaoService implements ITransService {

    /**
     * The Tx cache manager.
     */
    @Autowired
    private TransactionCacheManager txCacheManager;

    /**
     * The Utxo entity dao.
     */
    @Autowired
    private UTXOEntityDao utxoEntityDao;

    /**
     * The Transaction index entity dao.
     */
    @Autowired
    private TransactionIndexEntityDao transactionIndexEntityDao;

    /**
     * The Spent transaction out index entity dao.
     */
    @Autowired
    private SpentTransactionOutIndexEntityDao spentTransactionOutIndexEntityDao;

    /**
     * The Balance entity dao.
     */
    @Autowired
    private IBalanceEntity balanceEntityDao;

    @Override
    public void addTransIdxAndUtxo(Block bestBlock, String bestBlockHash) {

        List<Transaction> transactionList = bestBlock.getTransactions();
        final int txSize = transactionList.size();
        for (int txCount = 0; txCount < txSize; txCount++) {
            Transaction tx = transactionList.get(txCount);

            //add new tx index
            saveTxIndex(tx, bestBlockHash, txCount);

            List<TransactionInput> inputList = tx.getInputs();

            if (CollectionUtils.isNotEmpty(inputList)) {
                for (TransactionInput input : inputList) {
                    TransactionOutPoint outPoint = input.getPrevOut();
                    String spentTxHash = outPoint.getHash();
                    short spentTxOutIndex = outPoint.getIndex();

                    TransactionIndex txIndex = getTransactionIndex(spentTxHash);
                    if (txIndex == null) {
                        throw new IllegalStateException("Spent tx not exits: " + spentTxHash);
                    }
                    //update the pre-transaction state
                    SpentTransactionOutIndexEntity spentTxOutIndexEntity = new SpentTransactionOutIndexEntity();
                    spentTxOutIndexEntity.setPreTransactionHash(spentTxHash);
                    spentTxOutIndexEntity.setOutIndex(spentTxOutIndex);
                    spentTxOutIndexEntity.setNowTransactionHash(tx.getHash());
                    spentTransactionOutIndexEntityDao.add(spentTxOutIndexEntity);
                    //remove spent utxo
                    String utxoKey = UTXO.buildKey(spentTxHash, spentTxOutIndex);
                    if (getUTXO(utxoKey) == null) {
                        throw new IllegalStateException("UTXO not exists : " + utxoKey);
                    }
                    utxoEntityDao.delete(spentTxHash, spentTxOutIndex);
                }
            }

            //add new utxo
            List<TransactionOutput> outputs = tx.getOutputs();
            if (CollectionUtils.isNotEmpty(outputs)) {
                final int outputSize = outputs.size();
                for (int i = 0; i < outputSize; i++) {
                    TransactionOutput output = outputs.get(i);
                    UTXO utxo = new UTXO(tx, (short) i, output);
                    saveUTXO(utxo);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
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

                UTXO utxo = getUTXO(preUTXOKey);
                if (utxo == null) {
                    txCacheManager.remove(tx.getHash());
                    cacheTransactions.remove(i);
                    LOGGER.warn("utxo data map has no this uxto={}_tx={}", preUTXOKey, tx.getHash());
                    break;
                }

                spentUTXOMap.put(preUTXOKey, tx.getHash());
            }
        }
    }

    /**
     * Compute miner balance.
     *
     * @param block the best block
     */
    @Override
    public void computeMinerBalance(Block block) {
        Map<String, Money> balanceMap = balanceEntityDao.getAllBalances();
        Map<String, Money> balanceAddMap = Maps.newHashMap();
        for (Transaction transaction : block.getTransactions()) {
            for (TransactionInput input : transaction.getInputs()) {
                String spentTxHash = input.getPrevOut().getHash();
                short spentTxOutIndex = input.getPrevOut().getIndex();
                String utxoKey = UTXO.buildKey(spentTxHash, spentTxOutIndex);
                UTXO utxo = getUTXO(utxoKey);
                if(null == utxo){
                    throw new IllegalStateException("UTXO not exists : " + utxoKey);
                }

                if (!utxo.getOutput().isMinerCurrency()) {
                    continue;
                }

                Money present = balanceMap.get(utxo.getAddress());
                if (null != present) {
                    balanceMap.put(utxo.getAddress(), present.subtract(utxo.getOutput().getMoney()));
                }
            }

            for (TransactionOutput output : transaction.getOutputs()) {
                if (!output.isMinerCurrency()) {
                    continue;
                }

                String address = output.getLockScript().getAddress();
                Money present = balanceMap.get(address);
                if (null != present) {
                    balanceMap.put(address, present.add(output.getMoney()));
                } else {
                    balanceAddMap.put(address, output.getMoney());
                }
            }
        }

        LOGGER.info("compute balance info：{},balanceAddMap:{}", balanceMap, balanceAddMap);
        balanceMap.forEach((k, v) -> {
            if (v.lessThan(0)) {
                balanceEntityDao.delete(k);
            } else {
                balanceEntityDao.update(new BalanceEntity(k, v.getValue(), v.getCurrency()));
            }
        });
        balanceAddMap.forEach((k, v) -> balanceEntityDao.add(new BalanceEntity(k, v.getValue(), v.getCurrency())));
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

        transactionIndexEntityDao.add(transactionIndexEntity);
    }

    /**
     * Gets transaction index.
     *
     * @param spentTxHash the spent tx hash
     * @return the transaction index
     */
    private TransactionIndex getTransactionIndex(String spentTxHash) {
        TransactionIndexEntity entity = transactionIndexEntityDao.getByField(spentTxHash);
        if (entity == null) {
            return null;
        }

        TransactionIndex transactionIndex = new TransactionIndex();
        transactionIndex.setBlockHash(entity.getBlockHash());
        transactionIndex.setTxHash(entity.getTransactionHash());
        transactionIndex.setTxIndex(entity.getTransactionIndex());

        return transactionIndex;
    }

    /**
     * Save utxo.
     *
     * @param utxo the utxo
     */
    private void saveUTXO(UTXO utxo) {
        UTXOEntity entity = new UTXOEntity();
        TransactionOutput output = utxo.getOutput();

        entity.setAmount(output.getMoney().getValue());
        entity.setScriptType(output.getLockScript().getType());
        entity.setTransactionHash(utxo.getHash());
        entity.setOutIndex(utxo.getIndex());
        entity.setCurrency(output.getMoney().getCurrency());
        entity.setLockScript(output.getLockScript().getAddress());

        utxoEntityDao.add(entity);
    }

    @Override
    public UTXO getUTXO(String utxoKey) {
        String[] keys = utxoKey.split("_");
        UTXOEntity entity = utxoEntityDao.getByField(keys[0], Short.valueOf(keys[1]));

        if (entity == null) {
            return null;
        }

        TransactionOutput output = new TransactionOutput();

        LockScript lockScript = new LockScript();
        lockScript.setAddress(entity.getLockScript());
        lockScript.setType((short) entity.getScriptType());

        output.setMoney(new Money(entity.getAmount(), entity.getCurrency()));
        output.setLockScript(lockScript);

        UTXO utxo = new UTXO();
        utxo.setAddress(lockScript.getAddress());
        utxo.setHash(entity.getTransactionHash());
        utxo.setIndex(entity.getOutIndex());
        utxo.setOutput(output);

        return utxo;
    }
}
