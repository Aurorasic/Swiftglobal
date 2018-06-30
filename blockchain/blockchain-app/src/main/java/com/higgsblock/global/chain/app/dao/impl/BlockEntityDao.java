package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.chain.app.dao.entity.BlockEntity;
import com.higgsblock.global.chain.app.dao.iface.IBlockEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-08
 */
@Repository
public class BlockEntityDao extends BaseDao<BlockEntity> implements IBlockEntity {

    @Override
    public int add(BlockEntity blockEntity) {
        String sql = "insert into t_block values (:blockHash,:height,:data)";
        return super.add(blockEntity, sql);
    }

    @Override
    public int update(BlockEntity blockEntity) {
        String sql = "update t_block set height =:height,data=:data where block_hash=:blockHash";
        return super.update(blockEntity, sql);
    }

    @Override
    public <E> int delete(E height) {
        String sql = "delete from t_block where height = :height";
        return super.delete(sql, ImmutableMap.of("height", height));
    }

    @Override
    public <E> BlockEntity getByField(E blockHash) {
        String sql = "select block_hash,height,data from t_block where block_hash = :blockHash";
        return super.getByField(sql,ImmutableMap.of("blockHash", blockHash));
    }

    @Override
    public List<BlockEntity> findAll() {
        String sql = "select block_hash,height,data from t_block";
        return super.findAll(sql);
    }

    @Override
    public long getBlockCount() {
        String sql = "select count(height) from t_block";
        return template.getJdbcOperations().queryForObject(sql, Long.class);
    }

}
