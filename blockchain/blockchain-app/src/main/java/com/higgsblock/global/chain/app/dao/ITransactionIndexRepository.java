package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author yangshenghong
 * @date 2018-07-13
 */
public interface ITransactionIndexRepository extends JpaRepository<TransactionIndexEntity, Long> {

    @Override
    @CachePut(value = "TransactionIndex", key = "#entity.transactionHash", condition = "null != #entity && null != #entity.transactionHash")
    TransactionIndexEntity save(TransactionIndexEntity entity);

    /**
     * find by txHash
     *
     * @param txHash
     * @return
     */
    @Cacheable(value = "TransactionIndex", key = "#txHash", condition = "null != #txHash")
    TransactionIndexEntity findByTransactionHash(String txHash);
}
