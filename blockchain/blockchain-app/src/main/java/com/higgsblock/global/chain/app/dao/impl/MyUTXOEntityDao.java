package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.chain.app.dao.entity.MyUTXOEntity;
import com.higgsblock.global.chain.app.dao.iface.IMyUTXOEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-09
 */
@Repository
public class MyUTXOEntityDao extends BaseDao<MyUTXOEntity> implements IMyUTXOEntity {
    @Override
    public int add(MyUTXOEntity myUTXOEntity) {
        String sql = "insert into t_my_utxo values (:transactionHash,:outIndex,:amount,:currency,:scriptType,:lockScript);";
        return super.add(myUTXOEntity, sql);
    }

    @Override
    public int update(MyUTXOEntity myUTXOEntity) {
        String sql = "update t_my_utxo set amount=:amount,currency=:currency,script_type=:scriptType," +
                "lock_script=:lockScript where transaction_hash=:transactionHash and out_index=:outIndex";
        return super.update(myUTXOEntity, sql);
    }

    @Override
    public int delete(String transactionHash, short outIndex) {
        String sql = "delete from t_my_utxo where transaction_hash=:transactionHash and out_index=:outIndex";
        return super.delete(sql, ImmutableMap.of("transactionHash", transactionHash, "outIndex", outIndex));
    }

    @Override
    public MyUTXOEntity getByField(String transactionHash, short outIndex) {
        String sql = "select transaction_hash,out_index,amount,currency,script_type,lock_script from" +
                " t_my_utxo where transaction_hash=:transactionHash and out_index=:outIndex";
        return super.getByField(sql, ImmutableMap.of("transactionHash", transactionHash, "outIndex", outIndex));
    }


    @Override
    public List<MyUTXOEntity> findAll() {
        String sql = "select transaction_hash,out_index,amount,currency,script_type,lock_script from t_my_utxo";
        return super.findAll(sql);
    }

    @Override
    public <E> int delete(E e) {
        return 0;
    }

    @Override
    public <E> MyUTXOEntity getByField(E e) {
        return null;
    }

}
