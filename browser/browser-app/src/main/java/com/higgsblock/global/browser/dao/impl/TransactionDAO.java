package com.higgsblock.global.browser.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.browser.dao.entity.TransactionPO;
import com.higgsblock.global.browser.dao.iface.ITransactionDAO;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
@Repository
public class TransactionDAO extends BaseDAO<TransactionPO> implements ITransactionDAO {
    @Override
    public int add(TransactionPO transactionPo) {
        return 0;
    }

    @Override
    public int update(TransactionPO transactionPo) {
        return 0;
    }

    @Override
    public <E> int delete(E e) {
        return 0;
    }

    @Override
    public <E> List<TransactionPO> getByField(E transactionHash) {
        String sql = "select block_hash,version,locktime,extra,transaction_hash from t_transaction where transaction_hash=:transactionHash";
        return super.getByField(sql, ImmutableMap.of("transactionHash", transactionHash));
    }

    @Override
    public List<TransactionPO> findByPage(int start, int limit) {
        String sql = "select block_hash,version,locktime,extra,transaction_hash from t_transaction limit :start,:limit";
        return super.findByPage(ImmutableMap.of("start", start, "limit", limit), sql);
    }

    @Override
    public int[] batchInsert(List<TransactionPO> transactionPos) {
        String sql = "insert into t_transaction values (:id,:height,:blockHash,:transactionHash,:version,:lockTime,:extra)";
        return super.template.batchUpdate(sql, SqlParameterSourceUtils.createBatch(transactionPos.toArray()));
    }

    @Override
    public List<String> getTxHashByBlockHash(String blockHash) {
        String sql = "select transaction_hash from t_transaction where block_hash=:blockHash";
        return super.template.queryForList(sql, ImmutableMap.of("blockHash", blockHash), String.class);
    }
}
