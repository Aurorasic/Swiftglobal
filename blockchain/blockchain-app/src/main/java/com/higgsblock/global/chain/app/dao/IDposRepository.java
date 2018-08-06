package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.DposEntity;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IDposRepository extends JpaRepository<DposEntity, Long> {

    @Override
    @CachePut(value = "Dpos", key = "#entity.sn", condition = "null != #entity && null != #entity.sn")
    DposEntity save(DposEntity entity);

    /**
     * find DposEntity by sn
     *
     * @param sn
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    @Cacheable(value = "Dpos", key = "#sn", condition = "#sn > 0")
    DposEntity findBySn(long sn);

}
