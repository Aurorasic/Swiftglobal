package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;
import com.higgsblock.global.chain.app.dao.iface.IScoreEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yuanjiantao
 * @date 6/30/2018
 */
@Repository
public class ScoreEntityDao extends BaseDao<ScoreEntity> implements IScoreEntity {
    @Override
    public int add(ScoreEntity scoreEntity) {
        String sql = "insert into t_score (address,score)values (:address,:score)";
        return super.add(scoreEntity, sql);
    }

    @Override
    public int update(ScoreEntity scoreEntity) {
        String sql = "update t_score set score=:score where address=:address";
        return super.update(scoreEntity, sql);
    }

    @Override
    public int updateBatch(List<String> addressList, int score) {
        String sql = "update t_score set score=:score where address in (:addresses)";
        return template.update(sql, ImmutableMap.of("addresses", addressList, "score", score));
    }

    @Override
    public <E> int delete(E address) {
        String sql = "delete from t_score where address=:address";
        return super.delete(sql, ImmutableMap.of("address", address));
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

    @Override
    public int plusScore(int score) {
        String sql = "update t_score set score=score+:score";
        return template.update(sql, ImmutableMap.of("score", score));
    }
}
