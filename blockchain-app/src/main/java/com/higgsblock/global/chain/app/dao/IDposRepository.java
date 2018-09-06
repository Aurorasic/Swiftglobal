package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.DposEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.IndexQuery;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IDposRepository extends JpaRepository<DposEntity, Long> {

    @Override
    @CachePut(value = "Dpos", key = "#p0.sn", condition = "null != #p0 && null != #p0.sn")
    DposEntity save(DposEntity entity);

    /**
     * find DposEntity by sn
     *
     * @param sn
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    @IndexQuery("sn")
    @Cacheable(value = "Dpos", key = "#p0", condition = "#p0 > 0", unless = "#result == null")
    DposEntity findBySn(long sn);

}
