package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionIndex;
import com.higgsblock.global.chain.app.dao.entity.TransactionIndexEntity;
import com.higgsblock.global.chain.app.dao.iface.ITransactionIndexEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-08
 */
@Repository
public class TransactionIndexEntityDao extends BaseDao<TransactionIndexEntity> implements ITransactionIndexEntity {
    @Override
    public int add(TransactionIndexEntity transactionIndexEntity) {
        String sql = "insert into t_transaction_index (transaction_hash,block_hash,transaction_index)values (:transactionHash,:blockHash,:transactionIndex)";
        return super.add(transactionIndexEntity, sql);
    }

    @Override
    public int update(TransactionIndexEntity transactionIndexEntity) {
        String sql = "update t_transaction_index set block_hash=:blockHash,transaction_index=:transactionIndex where transaction_hash = :transactionHash";
        return super.add(transactionIndexEntity, sql);
    }

    @Override
    public <E> int delete(E transactionHash) {
        String sql = "delete from t_transaction_index where transaction_hash = :transactionHash";
        return super.delete(sql, ImmutableMap.of("transactionHash", transactionHash));
    }

    @Override
    public <E> TransactionIndexEntity getByField(E transactionHash) {
        String sql = "select transaction_hash,block_hash,transaction_index from t_transaction_index where transaction_hash=:transactionHash";
        return super.getByField(sql, ImmutableMap.of("transactionHash", transactionHash));
    }

    @Override
    public List<TransactionIndexEntity> findAll() {
        String sql = "select transaction_hash,block_hash,transaction_index from t_transaction_index";
        return super.findAll(sql);
    }

    @Override
    public TransactionIndex get(String transactionHash) {
        TransactionIndexEntity entity = getByField(transactionHash);
        return entity != null ? new TransactionIndex(entity.getBlockHash(), entity.getTransactionHash(), entity.getTransactionIndex()) : null;
    }
}
