package com.higgsblock.global.browser.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.browser.dao.entity.MinerPO;
import com.higgsblock.global.browser.dao.iface.IMinersDAO;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-21
 */
@Repository
public class MinersDAO extends BaseDAO<MinerPO> implements IMinersDAO {
    @Override
    public int add(MinerPO minerPo) {
        String sql = "insert into t_miners values (:id,:amount, :address)";
        return super.add(minerPo, sql);
    }

    @Override
    public int update(MinerPO minerPo) {
        String sql = "update t_miners set amount=:amount where address=:address";
        return super.update(minerPo, sql);
    }

    @Override
    public <E> int delete(E address) {
        String sql = "delete from t_miners where address=:address";
        return super.delete(sql, ImmutableMap.of("address", address));
    }

    @Override
    public <E> List<MinerPO> getByField(E address) {
        String sql = "select amount, address from t_miners where address=:address";
        return super.getByField(sql, ImmutableMap.of("address", address));
    }

    @Override
    public List<MinerPO> findByPage(int start, int limit) {
        String sql = "select amount, address from t_miners limit :start,:limit";
        return super.findByPage(ImmutableMap.of("start", start, "limit", limit), sql);
    }

    @Override
    public long getMinersCount() {
        try {
            String sql = "select count(address) from t_miners where amount>=1";
            return super.template.getJdbcOperations().queryForObject(sql, Long.class);
        } catch (DataAccessException e) {
            return 0;
        }
    }

    @Override
    public int[] batchInsert(List<MinerPO> miners) {
        return new int[0];
    }

    @Override
    public int[] batchSaveOrUpdate(List<MinerPO> miners) {
        String sql = "insert into t_miners values (:id,:amount,:address) on duplicate key update amount = :amount";
        return super.template.batchUpdate(sql, SqlParameterSourceUtils.createBatch(miners.toArray()));
    }

    @Override
    public int[] batchDeleteMiners(List<String> address) {
        String sql = "delete from t_miners where address=?";
        return super.template.getJdbcOperations().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setString(1, address.get(i));
            }

            @Override
            public int getBatchSize() {
                return address.size();
            }
        });
    }
}
