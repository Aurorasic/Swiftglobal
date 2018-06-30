package com.higgsblock.global.chain.app.api.service;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import com.higgsblock.global.chain.app.dao.impl.UTXOEntityDao;
import com.higgsblock.global.chain.app.script.LockScript;
import com.higgsblock.global.chain.common.utils.Money;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author kongyu
 * @date 2018-03-19
 */
@Slf4j
@Service
public class UTXORespService {

    @Autowired
    private UTXOEntityDao utxoEntityDao;

    /**
     * Query the corresponding UTXOS according to the address information
     *
     * @param addr
     * @return
     */
    public List<UTXO> getUTXOsByAddress(String addr) {
        if (null == addr) {
            throw new RuntimeException("addr is null");
        }

        List<UTXOEntity> entityList = utxoEntityDao.selectByAddress(addr);
        if (CollectionUtils.isEmpty(entityList)) return null;

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
}
