package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.DictionaryEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IBlockInfoRepository extends CrudRepository<DictionaryEntity, Long> {

    @Override
    @CachePut(value = "BlockInfo", key = "#p0.id", condition = "null != #p0 && null != #p0.id")
    @CacheEvict(value = "BlockInfo", key = "#p0.id", condition = "null != #p0")
    DictionaryEntity save(DictionaryEntity entity);

    @Cacheable(value = "BlockInfo", key = "#p0", condition = "null != #p0", unless = "#result != null")
    DictionaryEntity findOne(String id);
}
