package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.chain.app.dao.entity.SpentTransactionOutIndexEntity;
import com.higgsblock.global.chain.app.dao.iface.ISpentTransactionOutIndexEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-12
 */
@Repository
public class SpentTransactionOutIndexEntityDao extends BaseDao<SpentTransactionOutIndexEntity> implements ISpentTransactionOutIndexEntity {
    @Override
    public int add(SpentTransactionOutIndexEntity spentTransactionOutIndexEntity) {
        String sql = "insert into t_spent_transaction_out_index (pre_transaction_hash,out_index,now_transaction_hash)values (:preTransactionHash,:outIndex,:nowTransactionHash)";
        return super.add(spentTransactionOutIndexEntity, sql);
    }

    @Override
    public List<SpentTransactionOutIndexEntity> getByPreHash(String preTxHash) {
        List<SpentTransactionOutIndexEntity> spentTxOutIndexEntities = null;
        String sql = "select pre_transaction_hash,out_index,now_transaction_hash from t_spent_transaction_out_index where pre_transaction_hash=:preTransactionHash";
        try {
            spentTxOutIndexEntities = super.template.query(sql, ImmutableMap.of("preTransactionHash", preTxHash), new BeanPropertyRowMapper<>(SpentTransactionOutIndexEntity.class));
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return spentTxOutIndexEntities;
    }
}
