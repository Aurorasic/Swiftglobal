package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.Transactional;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-07-12
 */
public class IUTXORepositoryTest extends BaseTest {
    @Autowired
    private IUTXORepository utxoRepository;

    @Test
    public void save() {
        UTXOEntity entity = new UTXOEntity();
        entity.setTransactionHash("txHash");
        entity.setOutIndex((short) 3);
        entity.setAmount("amount");
        entity.setCurrency("currency");
        entity.setScriptType(3);
        entity.setLockScript("lockScript");
        utxoRepository.save(entity);
    }

    @Test
    public void findUTXOEntityByTransactionHashAndOutIndex() {
        UTXOEntity transactionHash = utxoRepository.findByTransactionHashAndOutIndex("txHash", (short) 3);
        System.err.println(transactionHash);
    }

    @Test
    public void findUTXOEntitiesByLockScript() {
        List<UTXOEntity> lockScript = utxoRepository.findByLockScript("lockScript1");
        for (UTXOEntity utxoEntity : lockScript) {
            System.err.println(utxoEntity);
        }
    }

    @Test
    public void findUTXOEntitiesByLockScriptAndCurrency() {
        List<UTXOEntity> utxoEntitiesByLockScriptAndCurrency = utxoRepository.findByLockScriptAndCurrency("lockScript", "currency");
        for (UTXOEntity utxoEntity : utxoEntitiesByLockScriptAndCurrency) {
            System.err.println(utxoEntity);
        }
    }

    @Test
    @Transactional
    @Rollback(false)
    public void deleteUTXOEntityByTransactionHashAndOutIndex() {
        utxoRepository.deleteByTransactionHashAndOutIndex("txHash", (short) 3);
    }
}
