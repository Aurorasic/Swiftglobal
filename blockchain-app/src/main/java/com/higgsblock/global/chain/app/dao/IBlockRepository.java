package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.BlockEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.IndexQuery;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018-08-08
 */
public interface IBlockRepository extends IKeyValueRepository<BlockEntity, Long> {

    @Override
    @CachePut(value = "Block", key = "#p0.blockHash", condition = "null != #p0 && null != #p0.blockHash")
    @CacheEvict(value = "Block", key = "#p0.height", condition = "null != #p0 && #p0.height > 0")
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
    @Cacheable(value = "Block", key = "#p0", condition = "null != #p0", unless = "#result == null")
    BlockEntity findByBlockHash(String blockHash);

    @IndexQuery("height")
    @Cacheable(value = "Block", key = "#p0", condition = "#p0 > 0", unless = "#result == null")
    List<BlockEntity> findByHeight(long height);


    /**
     * delete BlockEntity by height
     *
     * @param height
     * @return
     */
    @CacheEvict(value = "Block", allEntries = true)
    int deleteByHeight(long height);

}
