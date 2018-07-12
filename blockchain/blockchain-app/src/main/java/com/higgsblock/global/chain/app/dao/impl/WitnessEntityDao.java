package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.chain.app.dao.entity.WitnessEntity;
import com.higgsblock.global.chain.app.dao.iface.IWitnessEntity;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-06-30
 */
@Repository
public class WitnessEntityDao extends BaseDao<WitnessEntity> implements IWitnessEntity {
    @Override
    public int add(WitnessEntity witnessEntity) {
        return 0;
    }

    @Override
    public int update(WitnessEntity witnessEntity) {
        return 0;
    }

    @Override
    public <E> int delete(E e) {
        return 0;
    }

    @Override
    public <E> WitnessEntity getByField(E e) {
        String sql = "select id,pub_key,address,socket_port,http_port from t_witness where pub_key=:pubKey";
        return super.getByField(sql, ImmutableMap.of("pubKey", e));
    }

    @Override
    public List<WitnessEntity> findAll() {
        String sql = "select id,pub_key,address,socket_port,http_port from t_witness;";
        return super.findAll(sql);
    }

    @Override
    public int[] batchInsert(List<WitnessEntity> witnessEntities) {
        String sql = "insert into t_witness (id,pub_key,address,socket_port,http_port)values (:id,:pubKey,:address,:socketPort,:httpPort)";
        return super.template.batchUpdate(sql, SqlParameterSourceUtils.createBatch(witnessEntities.toArray()));
    }
}
