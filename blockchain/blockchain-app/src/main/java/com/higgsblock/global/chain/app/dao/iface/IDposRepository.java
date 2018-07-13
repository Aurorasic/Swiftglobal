package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.app.dao.entity.DposEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IDposRepository extends JpaRepository<DposEntity, Long> {

    /**
     * find DposEntity by sn
     *
     * @param sn
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    DposEntity findBySn(long sn);

}
