package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.chain.app.dao.entity.DposEntity;
import com.higgsblock.global.chain.app.dao.iface.IDposEntity;

import java.util.List;

/**
 * @author yuanjiantao
 * @date 6/30/2018
 */
public class DposEntityDao extends BaseDao<DposEntity> implements IDposEntity {
    @Override
    public int add(DposEntity dposEntity) {
        String sql = "insert into t_dpos values (:sn, :addresses)";
        return super.add(dposEntity, sql);
    }

    @Override
    public int update(DposEntity dposEntity) {
        String sql = "update t_dpos set addresses=:addresses where sn=:sn";
        return super.update(dposEntity, sql);
    }

    @Override
    public <E> int delete(E sn) {
        String sql = "delete from t_dpos where sn=:sn";
        return super.delete(sql, ImmutableMap.of("sn", sn));
    }

    @Override
    public <E> DposEntity getByField(E sn) {
        String sql = "select sn, addresses from t_dpos where sn=:sn";
        return super.getByField(sql, ImmutableMap.of("sn", sn));
    }

    @Override
    public List<DposEntity> findAll() {
        String sql = "select sn, addresses from t_dpos";
        return super.findAll(sql);
    }
}
