package com.higgsblock.global.browser.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.browser.dao.entity.TransactionInputPO;
import com.higgsblock.global.browser.dao.iface.ITransactionInputDAO;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
@Repository
public class TransactionInputDAO extends BaseDAO<TransactionInputPO> implements ITransactionInputDAO {
    @Override
    public int add(TransactionInputPO transactionInputPo) {
        return 0;
    }

    @Override
    public int update(TransactionInputPO transactionInputPo) {
        return 0;
    }

    @Override
    public <E> int delete(E e) {
        return 0;
    }

    @Override
    public <E> List<TransactionInputPO> getByField(E transactionHash) {
        String sql = "select transaction_hash,`index`,pre_out_index,address_list,pre_transaction_hash from t_transaction_input where transaction_hash = :transactionHash";
        return super.getByField(sql, ImmutableMap.of("transactionHash", transactionHash));
    }

    @Override
    public List<TransactionInputPO> findByPage(int start, int limit) {
        String sql = "select transaction_hash,`index`,pre_out_index,address_list,pre_transaction_hash from t_transaction_input limit :start,:limit";
        return super.findByPage(ImmutableMap.of("start", start, "limit", limit), sql);
    }

    @Override
    public int[] batchInsert(List<TransactionInputPO> transactionInputPos) {
        String sql = "insert into t_transaction_input values (:id,:transactionHash,:index,:preTransactionHash,:preOutIndex,:addressList)";
        return super.template.batchUpdate(sql, SqlParameterSourceUtils.createBatch(transactionInputPos.toArray()));
    }

    @Override
    public List<String> getTxHashsByPubKey(String pubKey) {
        String sql = "select transaction_hash from t_transaction_input where address_list like :addressList";
        pubKey = "%" + pubKey + "%";
        return super.template.queryForList(sql, ImmutableMap.of("addressList", pubKey), String.class);
    }
}
