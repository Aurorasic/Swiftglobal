package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.BlockChainInfoEntity;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IBlockChainInfoRepository extends IKeyValueRepository<BlockChainInfoEntity, String> {

    @Override
    @CachePut(value = "BlockInfo", key = "#p0.id", condition = "null != #p0 && null != #p0.id")
    @CacheEvict(value = "BlockInfo", key = "#p0.id", condition = "null != #p0")
    BlockChainInfoEntity save(BlockChainInfoEntity entity);

    @Override
    @Cacheable(value = "BlockInfo", key = "#p0", condition = "null != #p0", unless = "#result != null")
    BlockChainInfoEntity findOne(String id);
}
