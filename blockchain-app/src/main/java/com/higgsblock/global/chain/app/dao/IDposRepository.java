package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.DposEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.IndexQuery;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IDposRepository extends IKeyValueRepository<DposEntity, Long> {

    @Override
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
    DposEntity findBySn(long sn);

}
