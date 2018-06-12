package com.higgsblock.global.browser.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.browser.dao.entity.UTXOPO;
import com.higgsblock.global.browser.dao.iface.IUTXODAO;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-25
 */
@Repository
public class UTXODAO extends BaseDAO<UTXOPO> implements IUTXODAO {
    @Override
    public int add(UTXOPO utxoPo) {
        return 0;
    }

    @Override
    public int update(UTXOPO utxoPo) {
        return 0;
    }

    @Override
    public <E> int delete(E e) {
        return 0;
    }

    @Override
    public <E> List<UTXOPO> getByField(E address) {
        String sql = "select transaction_hash,out_index,amount,currency,script_type," +
                "address from t_utxo where address =:address";
        return super.getByField(sql, ImmutableMap.of("address", address));
    }

    @Override
    public List<UTXOPO> findByPage(int start, int limit) {
        return null;
    }

    @Override
    public int[] batchInsert(List<UTXOPO> utxoPos) {
        String sql = "insert into t_utxo values (:id,:transactionHash,:outIndex,:amount,:currency,:scriptType,:address)";
        return super.template.batchUpdate(sql, SqlParameterSourceUtils.createBatch(utxoPos.toArray()));
    }

    @Override
    public int deleteUTXO(String transactionHash, short index) {
        String sql = "delete from t_utxo where transaction_hash =:transactionHash and out_index =:index";
        return super.delete(sql, ImmutableMap.of("transactionHash", transactionHash, "index", index));
    }
}
