package com.higgsblock.global.browser.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.browser.dao.entity.TransactionOutputPO;
import com.higgsblock.global.browser.dao.iface.ITransactionOutputDAO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
@Repository
public class TransactionOutputDAO extends BaseDAO<TransactionOutputPO> implements ITransactionOutputDAO {
    @Override
    public int add(TransactionOutputPO transactionOutputPo) {
        return 0;
    }

    @Override
    public int update(TransactionOutputPO transactionOutputPo) {
        return 0;
    }

    @Override
    public <E> int delete(E e) {
        return 0;
    }

    @Override
    public <E> List<TransactionOutputPO> getByField(E transactionHash) {
        String sql = "select `index`,amount,currency,script_type,address,transaction_hash from t_transaction_output where transaction_hash = :transactionHash";
        return super.getByField(sql, ImmutableMap.of("transactionHash", transactionHash));
    }

    @Override
    public List<TransactionOutputPO> findByPage(int start, int limit) {
        String sql = "select `index`,amount,currency,script_type,address,transaction_hash from t_transaction_output limit :start,:limit";
        return super.findByPage(ImmutableMap.of("start", start, "limit", limit), sql);
    }

    @Override
    public int[] batchInsert(List<TransactionOutputPO> transactionOutputPos) {
        String sql = "insert into t_transaction_output values (:id,:transactionHash,:index,:amount,:currency,:scriptType,:address)";
        return super.template.batchUpdate(sql, SqlParameterSourceUtils.createBatch(transactionOutputPos.toArray()));
    }

    @Override
    public List<String> getTxHashsByAddress(String address) {
        String sql = "select transaction_hash from t_transaction_output where address = :address";
        return super.template.queryForList(sql, ImmutableMap.of("address", address), String.class);
    }

    @Override
    public List<TransactionOutputPO> getTxOutput(String transactionHash, short index) {
        String sql = "select `index`,amount,currency,script_type,address,transaction_hash from t_transaction_output " +
                "where transaction_hash =:transactionHash and `index` =:index";
        List<TransactionOutputPO> transactionOutputPos = null;
        try {
            transactionOutputPos = super.getByField(sql, ImmutableMap.of("transactionHash",
                    transactionHash, "index", index));
        } catch (DataAccessException e) {
            return null;
        }
        return transactionOutputPos;
    }
}
