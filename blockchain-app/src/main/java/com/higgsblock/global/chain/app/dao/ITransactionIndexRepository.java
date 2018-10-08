package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.IndexQuery;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;

/**
 * @author yangshenghong
 * @date 2018-07-13
 */
public interface ITransactionIndexRepository extends IKeyValueRepository<TransactionIndexEntity, Long> {

    @Override
    TransactionIndexEntity save(TransactionIndexEntity entity);

    /**
     * find by txHash
     *
     * @param txHash
     * @return
     */
    @IndexQuery("transactionHash")
    TransactionIndexEntity findByTransactionHash(String txHash);
}
