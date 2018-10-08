package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.BlockEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.IndexQuery;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018-08-08
 */
public interface IBlockRepository extends IKeyValueRepository<BlockEntity, Long> {

    @Override
    BlockEntity save(BlockEntity entity);

    /**
     * find BlockEntity by blockHash
     *
     * @param blockHash
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    @IndexQuery("blockHash")
    BlockEntity findByBlockHash(String blockHash);

    @IndexQuery("height")
    List<BlockEntity> findByHeight(long height);


    /**
     * delete BlockEntity by height
     *
     * @param height
     * @return
     */
    @IndexQuery("height")
    int deleteByHeight(long height);

}
