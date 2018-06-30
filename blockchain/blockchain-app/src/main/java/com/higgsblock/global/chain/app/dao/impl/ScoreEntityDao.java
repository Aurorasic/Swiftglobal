package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;
import com.higgsblock.global.chain.app.dao.iface.IScoreEntity;

import java.util.List;

/**
 * @author yuanjiantao
 * @date 6/30/2018
 */
public class ScoreEntityDao extends BaseDao<ScoreEntity> implements IScoreEntity {
    @Override
    public int add(ScoreEntity scoreEntity) {
        String sql = "insert into t_score values (:address,:score)";
        return super.add(scoreEntity, sql);
    }

    @Override
    public int update(ScoreEntity scoreEntity) {
        String sql = "update t_score set score=:score where address=:address";
        return super.update(scoreEntity, sql);
    }

    @Override
    public <E> int delete(E e) {
        String sql = "delete from t_score where block_hash=:blockHash";
        return super.delete(sql, ImmutableMap.of("blockHash", e));
    }

    @Override
    public <E> ScoreEntity getByField(E address) {
        String sql = "select address, score from t_score where address=:address";
        return super.getByField(sql, ImmutableMap.of("address", address));
    }

    @Override
    public List<ScoreEntity> findAll() {
        String sql = "select address, score from t_score";
        return super.findAll(sql);
    }
}
