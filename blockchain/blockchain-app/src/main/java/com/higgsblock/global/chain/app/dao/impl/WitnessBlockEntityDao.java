package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.chain.app.dao.entity.WitnessBlockEntity;
import com.higgsblock.global.chain.app.dao.iface.IWitnessBlockEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-09
 */
@Repository
public class WitnessBlockEntityDao extends BaseDao<WitnessBlockEntity> implements IWitnessBlockEntity {
    @Override
    public int add(WitnessBlockEntity witnessBlockEntity) {
        String sql = "insert into t_witness_block values (:height,:blockHash)";
        return super.add(witnessBlockEntity, sql);
    }

    @Override
    public int update(WitnessBlockEntity witnessBlockEntity) {
        String sql = "update t_witness_block set height=:height where block_hash=:blockHash";
        return super.update(witnessBlockEntity, sql);
    }

    @Override
    public <E> int delete(E blockHash) {
        String sql = "delete from t_witness_block where block_hash=:blockHash";
        return super.delete(sql, ImmutableMap.of("blockHash", blockHash));
    }

    @Override
    public <E> WitnessBlockEntity getByField(E height) {
        String sql = "select height, block_hash from t_witness_block where height=:height";
        return super.getByField(sql, ImmutableMap.of("height", height));
    }

    @Override
    public List<WitnessBlockEntity> findAll() {
        String sql = "select height, block_hash from t_witness_block";
        return super.findAll(sql);
    }

}
