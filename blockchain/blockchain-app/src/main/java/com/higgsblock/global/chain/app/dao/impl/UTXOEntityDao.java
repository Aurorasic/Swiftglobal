package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.chain.app.dao.entity.UTXOEntity;
import com.higgsblock.global.chain.app.dao.iface.IUTXOEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-09
 */
@Repository
public class UTXOEntityDao extends BaseDao<UTXOEntity> implements IUTXOEntity {
    @Override
    public int add(UTXOEntity utxoEntity) {
        String sql = "insert into t_utxo values (:transactionHash,:outIndex,:amount,:currency,:scriptType,:lockScript);";
        return super.add(utxoEntity, sql);
    }

    @Override
    public int update(UTXOEntity utxoEntity) {
        String sql = "update t_utxo set amount=:amount,currency=:currency,script_type=:scriptType," +
                "lock_script=:lockScript where transaction_hash=:transactionHash and out_index=:outIndex";
        return super.update(utxoEntity, sql);
    }

    @Override
    public int delete(String transactionHash, short outIndex) {
        String sql = "delete from t_utxo where transaction_hash=:transactionHash and out_index=:outIndex";
        return super.delete(sql, ImmutableMap.of("transactionHash", transactionHash, "outIndex", outIndex));
    }

    @Override
    public List<UTXOEntity> findAll() {
        String sql = "select transaction_hash,out_index,amount,currency,script_type,lock_script from t_utxo";
        return super.findAll(sql);
    }

    @Override
    public UTXOEntity getByField(String transactionHash, short outIndex) {
        String sql = "select transaction_hash,out_index,amount,currency,script_type,lock_script from" +
                " t_utxo where transaction_hash=:transactionHash and out_index=:outIndex";
        return super.getByField(sql, ImmutableMap.of("transactionHash", transactionHash, "outIndex", outIndex));
    }

    @Override
    public <E> int delete(E utxoEntity) {
        return 0;
    }

    @Override
    public <E> UTXOEntity getByField(E utxoEntity) {
        return null;
    }
}
