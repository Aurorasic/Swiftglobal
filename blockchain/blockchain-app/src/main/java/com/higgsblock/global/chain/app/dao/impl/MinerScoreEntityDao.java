package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.chain.app.dao.entity.MinerScoreEntity;
import com.higgsblock.global.chain.app.dao.iface.IMinerScoreEntity;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-08
 */
@Repository
public class MinerScoreEntityDao extends BaseDao<MinerScoreEntity> implements IMinerScoreEntity {

    public static final String T_DPOS_MINER_SCORE = "t_dpos_miner_score";

    public static final String T_LATEST_MINER_SCORE = "t_latest_miner_score";

    public static final String T_TMP_MINER_SCORE = "t_tmp_miner_score";

    @Override
    public int add(String table, MinerScoreEntity minerScoreEntity) {
        String sql = null;
        sql = getSql(table, sql);
        return super.add(minerScoreEntity, sql);
    }

    @Override
    public int update(String table, MinerScoreEntity minerScoreEntity) {
        String sql = null;
        if (table.equals(T_DPOS_MINER_SCORE)) {
            sql = "update t_dpos_miner_score set score=:score where address=:address";
        } else if (table.equals(T_LATEST_MINER_SCORE)) {
            sql = "update t_latest_miner_score set score=:score where address=:address";
        } else if (table.equals(T_TMP_MINER_SCORE)) {
            sql = "update t_tmp_miner_score set score=:score where address=:address";
        }
        return super.update(minerScoreEntity, sql);
    }

    @Override
    public int delete(String table, String address) {
        String sql = null;
        if (table.equals(T_DPOS_MINER_SCORE)) {
            sql = "delete from t_dpos_miner_score where address=:address";
        } else if (table.equals(T_LATEST_MINER_SCORE)) {
            sql = "delete from t_latest_miner_score where address=:address";
        } else if (table.equals(T_TMP_MINER_SCORE)) {
            sql = "delete from t_tmp_miner_score where address=:address";
        }
        return super.delete(sql, ImmutableMap.of("address", address));
    }


    @Override
    public MinerScoreEntity getByField(String table, String address) {
        String sql = null;
        if (table.equals(T_DPOS_MINER_SCORE)) {
            sql = "select address,score from t_dpos_miner_score where address=:address";
        } else if (table.equals(T_LATEST_MINER_SCORE)) {
            sql = "select address,score from t_latest_miner_score where address=:address";
        } else if (table.equals(T_TMP_MINER_SCORE)) {
            sql = "select address,score from t_tmp_miner_score where address=:address";
        }
        return super.getByField(sql, ImmutableMap.of("address", address));
    }

    @Override
    public List<MinerScoreEntity> findAll(String table) {
        String sql = null;
        if (table.equals(T_DPOS_MINER_SCORE)) {
            sql = "select address,score from t_dpos_miner_score";
        } else if (table.equals(T_LATEST_MINER_SCORE)) {
            sql = "select address,score from t_latest_miner_score";
        } else if (table.equals(T_TMP_MINER_SCORE)) {
            sql = "select address,score from t_tmp_miner_score";
        }
        return super.findAll(sql);
    }

    @Override
    public int[] batchInsert(String tableName, List<MinerScoreEntity> minerScoreEntities) {
        String sql = null;
        sql = getSql(tableName, sql);
        int[] ints = super.template.batchUpdate(sql, SqlParameterSourceUtils.createBatch(minerScoreEntities.toArray()));
        return ints;
    }


    @Override
    public int deleteAll(String tableName) {
        String sql = null;
        if (tableName.equals(T_DPOS_MINER_SCORE)) {
            sql = "delete from t_dpos_miner_score";
        } else if (tableName.equals(T_LATEST_MINER_SCORE)) {
            sql = "delete from t_latest_miner_score";
        } else if (tableName.equals(T_TMP_MINER_SCORE)) {
            sql = "delete from t_tmp_miner_score";
        }
        return super.delete(new MinerScoreEntity(), sql);
    }

    private String getSql(String tableName, String sql) {
        if (tableName.equals(T_DPOS_MINER_SCORE)) {
            sql = "insert into t_dpos_miner_score values (:address,:score)";
        } else if (tableName.equals(T_LATEST_MINER_SCORE)) {
            sql = "insert into t_latest_miner_score values (:address,:score)";
        } else if (tableName.equals(T_TMP_MINER_SCORE)) {
            sql = "insert into t_tmp_miner_score values (:address,:score)";
        }
        return sql;
    }
}
