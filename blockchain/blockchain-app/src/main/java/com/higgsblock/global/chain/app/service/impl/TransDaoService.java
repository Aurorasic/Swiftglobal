package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.transaction.*;
import com.higgsblock.global.chain.app.dao.entity.SpentTransactionOutIndexEntity;
import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import com.higgsblock.global.chain.app.dao.iface.ITransactionIndexRepository;
import com.higgsblock.global.chain.app.dao.iface.IUTXORepository;
import com.higgsblock.global.chain.app.dao.impl.SpentTransactionOutIndexEntityDao;
import com.higgsblock.global.chain.app.script.LockScript;
import com.higgsblock.global.chain.app.service.ITransService;
import com.higgsblock.global.chain.app.service.UTXODaoServiceProxy;
import com.higgsblock.global.chain.common.utils.Money;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The type Trans dao service.
 *
 * @author Zhao xiaogang
 * @date 2018 -05-22
 */
@Service
@Slf4j
public class TransDaoService implements ITransService {
    @Autowired
    private IUTXORepository iutxoRepository;

    @Autowired
    private UTXODaoServiceProxy utxoDaoServiceProxy;

    /**
     * The Transaction index entity dao.
     */
    @Autowired
    private ITransactionIndexRepository iTransactionIndexRepository;

    /**
     * The Spent transaction out index entity dao.
     */
    @Autowired
    private SpentTransactionOutIndexEntityDao spentTransactionOutIndexEntityDao;

    @Override
    public void addTransIdxAndUtxo(Block toBeBestBlock, String bestBlockHash) {

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
                    String spentTxHash = outPoint.getHash();
                    short spentTxOutIndex = outPoint.getIndex();

                    TransactionIndex txIndex = getTransactionIndex(spentTxHash);
                    if (txIndex == null) {
                        throw new IllegalStateException("Spent tx not exits: " + spentTxHash + toBeBestBlock.getSimpleInfoSuffix());
                    }
                    //update the pre-transaction state
                    SpentTransactionOutIndexEntity spentTxOutIndexEntity = new SpentTransactionOutIndexEntity();
                    spentTxOutIndexEntity.setPreTransactionHash(spentTxHash);
                    spentTxOutIndexEntity.setOutIndex(spentTxOutIndex);
                    spentTxOutIndexEntity.setNowTransactionHash(tx.getHash());
                    spentTransactionOutIndexEntityDao.add(spentTxOutIndexEntity);
                    //remove spent utxo
                    String utxoKey = UTXO.buildKey(spentTxHash, spentTxOutIndex);
                    if (utxoDaoServiceProxy.getUTXOOnBestChain(utxoKey) == null) {
                        throw new IllegalStateException("UTXO not exists : " + utxoKey + toBeBestBlock.getSimpleInfoSuffix());
                    }
                    deleteByTransactionHashAndOutIndex(spentTxHash, spentTxOutIndex);
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

                UTXO utxo = utxoDaoServiceProxy.getUnionUTXO(preBlockHash, preUTXOKey);
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

        iTransactionIndexRepository.save(transactionIndexEntity);
    }

    /**
     * Gets transaction index.
     *
     * @param spentTxHash the spent tx hash
     * @return the transaction index
     */
    private TransactionIndex getTransactionIndex(String spentTxHash) {
        TransactionIndexEntity entity = iTransactionIndexRepository.findByTransactionHash(spentTxHash);
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

        iutxoRepository.save(entity);
    }

    @Override
    public UTXO getUTXOOnBestChain(String utxoKey) {
        String[] keys = utxoKey.split("_");
        UTXOEntity entity = iutxoRepository.findByTransactionHashAndOutIndex(keys[0], Short.valueOf(keys[1]));

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

    @Override
    public List<UTXO> getUTXOsByAddress(String addr) {
        if (null == addr) {
            throw new RuntimeException("addr is null");
        }

        List<UTXOEntity> entityList = iutxoRepository.findByLockScript(addr);
        if (CollectionUtils.isEmpty(entityList)) {
            return null;
        }

        List<UTXO> utxos = Lists.newArrayList();
        entityList.forEach(entity -> {
            Money money = new Money(entity.getAmount(), entity.getCurrency());
            LockScript lockScript = new LockScript();
            lockScript.setAddress(entity.getLockScript());
            lockScript.setType((short) entity.getScriptType());
            TransactionOutput output = new TransactionOutput();
            output.setMoney(money);
            output.setLockScript(lockScript);

            UTXO utxo = new UTXO();
            utxo.setHash(entity.getTransactionHash());
            utxo.setIndex(entity.getOutIndex());
            utxo.setAddress(entity.getLockScript());
            utxo.setOutput(output);
            utxos.add(utxo);
        });
        return utxos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByTransactionHashAndOutIndex(String transactionHash, short outIndex) {
        iutxoRepository.deleteByTransactionHashAndOutIndex(transactionHash, outIndex);
    }
}
