package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.IUTXORepository;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.Transactional;
import com.higgsblock.global.chain.app.service.IBestUTXOService;
import com.higgsblock.global.chain.common.utils.Money;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Utxo service that only handle utxo on best chain blocks
 *
 * @author yuguojia
 * @date 2018/06/29
 **/
@Service
@Slf4j
public class BestUTXOService implements IBestUTXOService {
    @Autowired
    private IUTXORepository utxoRepository;

    @Override
    public void saveUTXO(UTXO utxo) {
        UTXOEntity entity = new UTXOEntity();
        TransactionOutput output = utxo.getOutput();

        entity.setId(entity.getTransactionHash() + entity.getOutIndex());
        entity.setAmount(output.getMoney().getValue());
        entity.setScriptType(output.getLockScript().getType());
        entity.setTransactionHash(utxo.getHash());
        entity.setOutIndex(utxo.getIndex());
        entity.setCurrency(output.getMoney().getCurrency());
        entity.setLockScript(output.getLockScript().getAddress());

        utxoRepository.save(entity);
    }

    /**
     * get utxo only on confirm block chain
     *
     * @param utxoKey
     * @return
     */
    @Override
    public UTXO getUTXOByKey(String utxoKey) {
        String[] keys = utxoKey.split("_");
        UTXOEntity entity = utxoRepository.findByTransactionHashAndOutIndex(keys[0], Short.valueOf(keys[1]));

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
    public List<UTXOEntity> findByLockScriptAndCurrency(String lockScript, String currency) {
        return utxoRepository.findByLockScriptAndCurrency(lockScript, currency);
    }

    @Override
    public List<UTXO> getUTXOsByAddress(String address) {
        if (StringUtils.isEmpty(address)) {
            throw new RuntimeException("address is empty for getUTXOsByAddress");
        }

        List<UTXO> utxos = Lists.newArrayList();
        List<UTXOEntity> entityList = utxoRepository.findByLockScript(address);
        if (CollectionUtils.isEmpty(entityList)) {
            return utxos;
        }

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteByTransactionHashAndOutIndex(String transactionHash, short outIndex) {
        utxoRepository.deleteByTransactionHashAndOutIndex(transactionHash, outIndex);
    }
}