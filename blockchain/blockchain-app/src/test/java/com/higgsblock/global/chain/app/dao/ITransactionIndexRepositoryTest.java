package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yangshenghong
 * @date 2018-07-13
 */
public class ITransactionIndexRepositoryTest extends BaseTest {

    @Autowired
    private ITransactionIndexRepository TransactionIndexRepository;

    @Test
    public void save() {
        TransactionIndexEntity transactionIndexEntity = new TransactionIndexEntity();
        transactionIndexEntity.setBlockHash("blockHash");
        transactionIndexEntity.setTransactionHash("transactionHash");
        transactionIndexEntity.setTransactionIndex((short) 1);
        TransactionIndexRepository.save(transactionIndexEntity);
    }

    @Test
    public void findByTxHash(){
        TransactionIndexEntity transactionHash = TransactionIndexRepository.findByTransactionHash("transactionHash");
        System.err.println(transactionHash);
    }
}
