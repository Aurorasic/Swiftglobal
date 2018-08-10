package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.BlockEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018-08-08
 */
public interface IBlockRepository extends JpaRepository<BlockEntity, Long> {

    @Override
    @CachePut(value = "Block", key = "#entity.blockHash", condition = "null != #entity && null != #entity.blockHash")
    @CacheEvict(value = "Block", key = "#entity.height", condition = "null != #entity && #entity.height > 0")
    BlockEntity save(BlockEntity entity);

    /**
     * find BlockEntity by blockHash
     *
     * @param blockHash
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    @Cacheable(value = "Block", key = "#blockHash", condition = "null != #blockHash")
    BlockEntity findByBlockHash(String blockHash);

    @Cacheable(value = "Block", key = "#height", condition = "#height > 0")
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
