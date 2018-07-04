package com.higgsblock.global.chain.app.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.chain.app.dao.entity.BlockIndexEntity;
import com.higgsblock.global.chain.app.dao.iface.IBlockIndexEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-08
 */
@Repository
public class BlockIndexEntityDao extends BaseDao<BlockIndexEntity> implements IBlockIndexEntity {
    @Override
    public int add(BlockIndexEntity blockIndexEntity) {
        String sql = "insert into t_block_index (height,block_hash,is_best,miner_address)values (:height,:blockHash,:isBest,:minerAddress)";
        return super.add(blockIndexEntity, sql);
    }

    @Override
    public int update(BlockIndexEntity blockIndexEntity) {
        String sql = "update t_block_index set is_best=:isBest where block_hash=:blockHash";
        return super.update(blockIndexEntity, sql);
    }

    @Override
    public <E> int delete(E blockHash) {
        String sql = "delete from t_block_index where block_hash=:blockHash";
        return super.delete(sql, ImmutableMap.of("blockHash", blockHash));
    }

    @Override
    public List<BlockIndexEntity> findAll() {
        String sql = "select height,block_hash,is_best,miner_address from t_block_index";
        return super.findAll(sql);
    }

    @Override
    public List<BlockIndexEntity> getAllByHeight(long height) {
        String sql = "select height,block_hash,is_best,miner_address from t_block_index where height=:height";
        return template.query(sql, ImmutableMap.of("height", height), new BeanPropertyRowMapper<>(BlockIndexEntity.class));
    }

    @Override
    public long getMaxHeight() {
        String sql = "select max(height) from t_block_index";
        return template.getJdbcOperations().queryForObject(sql, Long.class);
    }

    /**
     * load blockIndex by blockHash
     *
     * @param blockHash
     * @return
     */
    @Override
    public BlockIndexEntity getByBlockHash(String blockHash) {
        String sql = "select height,block_hash,is_best,miner_address from t_block_index where block_hash=:blockHash";
        return super.getByField(sql, ImmutableMap.of("blockHash", blockHash));
    }

    @Override
    public int[] insertBatch(List<BlockIndexEntity> blockIndexEntities) {
        String sql = "insert into t_block_index (height,block_hash,is_best,miner_address)values (:height,:blockHash,:isBest,:minerAddress)";
        return super.template.batchUpdate(sql, SqlParameterSourceUtils.createBatch(blockIndexEntities.toArray()));
    }

    @Override
    public <E> BlockIndexEntity getByField(E height) {
        return null;
    }

}
