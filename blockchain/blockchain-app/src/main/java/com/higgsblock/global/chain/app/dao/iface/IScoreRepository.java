package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IScoreRepository extends JpaRepository<ScoreEntity, Long> {

    /**
     * find ScoreEntity by address
     *
     * @author wangxiangyi
     * @date 2018/7/13
     */
    ScoreEntity findByAddress(String address);

    /**
     * delete ScoreEntity by address
     *
     * @author wangxiangyi
     * @date 2018/7/13
     */
    int deleteByAddress(String address);
}
