package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.BlockIndexEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.IndexQuery;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;

import java.util.List;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IBlockIndexRepository extends IKeyValueRepository<BlockIndexEntity, Long> {

    @Override
    BlockIndexEntity save(BlockIndexEntity entity);

    /**
     * find BlockIndexEntity by blockHash
     *
     * @param blockHash
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    @IndexQuery("blockHash")
    BlockIndexEntity findByBlockHash(String blockHash);

    /**
     * find BlockIndexEntities by height
     *
     * @param height
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    @IndexQuery("height")
    List<BlockIndexEntity> findByHeight(long height);

    /**
     * delete BlockIndexEntities by height
     *
     * @param height
     * @return
     */
    @IndexQuery("height")
    int deleteByHeight(long height);

}
