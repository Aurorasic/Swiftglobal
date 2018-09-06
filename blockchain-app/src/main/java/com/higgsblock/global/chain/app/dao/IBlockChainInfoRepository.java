package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.BlockChainInfoEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IBlockChainInfoRepository extends CrudRepository<BlockChainInfoEntity, Long> {

    @Override
    @CachePut(value = "BlockInfo", key = "#p0.id", condition = "null != #p0 && null != #p0.id")
    @CacheEvict(value = "BlockInfo", key = "#p0.id", condition = "null != #p0")
    BlockChainInfoEntity save(BlockChainInfoEntity entity);

    @Cacheable(value = "BlockInfo", key = "#p0", condition = "null != #p0", unless = "#result != null")
    BlockChainInfoEntity findOne(String id);
}
