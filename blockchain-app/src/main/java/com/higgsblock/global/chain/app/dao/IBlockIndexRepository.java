package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.BlockIndexEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.IndexQuery;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IBlockIndexRepository extends IKeyValueRepository<BlockIndexEntity, Long> {

    @Override
    @CachePut(value = "BlockIndex", key = "#p0.blockHash", condition = "null != #p0 && null != #p0.blockHash")
    @CacheEvict(value = "BlockIndex", key = "#p0.height", condition = "null != #p0")
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
    @Cacheable(value = "BlockIndex", key = "#p0", condition = "null != #p0", unless = "#result == null")
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
    @Cacheable(value = "BlockIndex", key = "#p0", condition = "#p0 > 0", unless = "#result == null")
    List<BlockIndexEntity> findByHeight(long height);

    /**
     * delete BlockIndexEntities by height
     *
     * @param height
     * @return
     */
    @IndexQuery("height")
    @CacheEvict(value = "BlockIndex", allEntries = true)
    int deleteByHeight(long height);

}
