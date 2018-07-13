package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import com.higgsblock.global.chain.app.dao.iface.IUTXOEntityRepository;
import com.higgsblock.global.chain.app.service.ITransService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-07-12
 */
public class IUTXOEntityRepositoryTest extends BaseTest {
    @Autowired
    private IUTXOEntityRepository iutxoEntityRepository;

    @Test
    public void save() {
        UTXOEntity entity = new UTXOEntity();
        entity.setTransactionHash("transactionHash");
        entity.setOutIndex((short) 2);
        entity.setAmount("amount");
        entity.setCurrency("currency");
        entity.setScriptType(3);
        entity.setLockScript("lockScript");
        iutxoEntityRepository.save(entity);
    }

    @Test
    public void findUTXOEntityByTransactionHashAndOutIndex() {
        UTXOEntity transactionHash = iutxoEntityRepository.findByTransactionHashAndOutIndex("txHash", (short) 3);
        System.err.println(transactionHash);
    }

    @Test
    public void findUTXOEntitiesByLockScript() {
        List<UTXOEntity> lockScript = iutxoEntityRepository.findByLockScript("lockScript1");
        for (UTXOEntity utxoEntity : lockScript) {
            System.err.println(utxoEntity);
        }
    }

    @Test
    public void findUTXOEntitiesByLockScriptAndCurrency() {
        List<UTXOEntity> utxoEntitiesByLockScriptAndCurrency = iutxoEntityRepository.findByLockScriptAndCurrency("lockScript", "currency");
        for (UTXOEntity utxoEntity : utxoEntitiesByLockScriptAndCurrency) {
            System.err.println(utxoEntity);
        }
    }

    @Test
    @Transactional
    @Rollback(false)
    public void deleteUTXOEntityByTransactionHashAndOutIndex() {
        iutxoEntityRepository.deleteByTransactionHashAndOutIndex("txHash", (short) 3);
    }
}
