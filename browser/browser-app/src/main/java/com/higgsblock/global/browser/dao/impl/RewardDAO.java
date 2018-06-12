package com.higgsblock.global.browser.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.browser.dao.entity.RewardPO;
import com.higgsblock.global.browser.dao.iface.IRewardDAO;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-24
 */
@Repository
public class RewardDAO extends BaseDAO<RewardPO> implements IRewardDAO {

    @Override
    public int[] batchInsert(List<RewardPO> rewardPos) {
        String sql = "insert into t_reward values (:id, :height, :address, :blockHash, :amount, :fee, :currency, :type)";
        return super.template.batchUpdate(sql, SqlParameterSourceUtils.createBatch(rewardPos.toArray()));
    }

    @Override
    public List<RewardPO> findByPage(String address, int start, int limit) {
        String sql = "select height,address, block_hash,amount,fee,currency,type " +
                "from t_reward where address=:address limit :start,:limit";
        return super.findByPage(ImmutableMap.of("address", address, "start", start, "limit", limit), sql);
    }

    @Override
    public long countByAddress(String address) {
        String sql = "select count(address) from t_reward where address = :address";
        try {
            return super.template.queryForObject(sql, ImmutableMap.of("address", address), Long.class);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int add(RewardPO rewardPos) {
        String sql = "insert into t_reward values (:id, :height, :address, :blockHash, :amount, :fee, :currency, :type)";
        return super.add(rewardPos, sql);
    }

    @Override
    public int update(RewardPO rewardPos) {
        return 0;
    }

    @Override
    public <E> int delete(E e) {
        return 0;
    }

    @Override
    public <E> List<RewardPO> getByField(E address) {
        String sql = "select height,address, block_hash,amount,fee,currency,type from t_reward where address=:address";
        return super.getByField(sql, ImmutableMap.of("address", address));
    }

    @Override
    public List<RewardPO> findByPage(int start, int limit) {
        return null;
    }
}
