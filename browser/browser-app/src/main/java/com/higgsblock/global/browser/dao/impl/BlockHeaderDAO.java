package com.higgsblock.global.browser.dao.impl;

import com.google.common.collect.ImmutableMap;
import com.higgsblock.global.browser.dao.entity.BlockHeaderPO;
import com.higgsblock.global.browser.dao.iface.IBlockHeaderDAO;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-21
 */
@Repository
public class BlockHeaderDAO extends BaseDAO<BlockHeaderPO> implements IBlockHeaderDAO {
    @Override
    public int add(BlockHeaderPO blockHeaderPo) {
        String sql = "insert into t_block_header values (:id,:height,:blockHash,:blockTime,:preBlockHash,:minerAddress," +
                ":witnessAddress,:txNum,:blockSize)";
        return super.add(blockHeaderPo, sql);
    }

    @Override
    public int update(BlockHeaderPO blockHeaderPo) {
        String sql = "update t_block_header set height=:height,block_time=:blockTime,pre_block_hash" +
                "=:preBlockHash,miner_address=:minerAddress,witness_address=:witnessAddress,tx_num=:txNum," +
                ":block_size=blockSize where block_hash=:blockHash";
        return super.update(blockHeaderPo, sql);
    }

    @Override
    public <E> int delete(E blockHash) {
        String sql = "delete from t_block_header where block_hash=:blockHash";
        return super.delete(sql, ImmutableMap.of("blockHash", blockHash));
    }

    @Override
    public <E> List<BlockHeaderPO> getByField(E blockHash) {
        String sql = "select height, block_hash,block_time,pre_block_hash,miner_address,witness_address,block_size from t_block_header where block_hash=:blockHash";
        return super.getByField(sql, ImmutableMap.of("blockHash", blockHash));
    }

    @Override
    public List<BlockHeaderPO> findByPage(int start, int limit) {
        String sql = "select height, block_hash,block_time,pre_block_hash,miner_address,witness_address," +
                "tx_num, block_size from t_block_header limit :start,:limit";
        return super.findByPage(ImmutableMap.of("start", start, "limit", limit), sql);
    }

    @Override
    public long getMaxHeight() {
        String sql = "select MAX(height) from t_block_header";
        try {
            return super.template.getJdbcOperations().queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int[] batchInsert(List<BlockHeaderPO> blockHeaderPos) {
        String sql = "insert into t_block_header values (:id,:height,:blockHash,:blockTime,:preBlockHash,:minerAddress," +
                ":witnessAddress,:txNum,:blockSize)";
        return super.template.batchUpdate(sql, SqlParameterSourceUtils.createBatch(blockHeaderPos.toArray()));
    }

    @Override
    public long getAllBlockHeaderSize() {
        String sql = "select COUNT(height) from t_block_header";
        try {
            return super.template.getJdbcOperations().queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public List<BlockHeaderPO> getScopeBlock(long start, long limit, String orderBy) {
        String sql = "select height, block_hash,block_time,pre_block_hash,miner_address,witness_address,tx_num, block_size from t_block_header " +
                "where height BETWEEN :start and :limit ORDER BY height " + orderBy;
        try {
            return super.findByPage(ImmutableMap.of("start", start, "limit", limit), sql);
        } catch (Exception e) {
            return null;
        }
    }
}
