package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-07-12
 */
public class IUTXORepositoryTest extends BaseTest {
    @Autowired
    private IUTXORepository iutxoRepository;

    @Test
    public void save() {
        UTXOEntity entity = new UTXOEntity();
        entity.setTransactionHash("txHash");
        entity.setOutIndex((short) 3);
        entity.setAmount("amount");
        entity.setCurrency("currency");
        entity.setScriptType(3);
        entity.setLockScript("lockScript");
        iutxoRepository.save(entity);
    }

    @Test
    public void findUTXOEntityByTransactionHashAndOutIndex() {
        UTXOEntity transactionHash = iutxoRepository.findByTransactionHashAndOutIndex("txHash", (short) 3);
        System.err.println(transactionHash);
    }

    @Test
    public void findUTXOEntitiesByLockScript() {
        List<UTXOEntity> lockScript = iutxoRepository.findByLockScript("lockScript1");
        for (UTXOEntity utxoEntity : lockScript) {
            System.err.println(utxoEntity);
        }
    }

    @Test
    public void findUTXOEntitiesByLockScriptAndCurrency() {
        List<UTXOEntity> utxoEntitiesByLockScriptAndCurrency = iutxoRepository.findByLockScriptAndCurrency("lockScript", "currency");
        for (UTXOEntity utxoEntity : utxoEntitiesByLockScriptAndCurrency) {
            System.err.println(utxoEntity);
        }
    }

    @Test
    @Transactional
    @Rollback(false)
    public void deleteUTXOEntityByTransactionHashAndOutIndex() {
        iutxoRepository.deleteByTransactionHashAndOutIndex("txHash", (short) 3);
    }
}
