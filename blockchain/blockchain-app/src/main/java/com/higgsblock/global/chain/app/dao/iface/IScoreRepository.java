package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IScoreRepository extends JpaRepository<ScoreEntity, Long> {

    /**
     * find ScoreEntity by address
     *
     * @param address
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    ScoreEntity findByAddress(String address);

    /**
     * delete ScoreEntity by address
     *
     * @param address
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    int deleteByAddress(String address);

    /**
     * update score by addresses
     *
     * @param addresses
     * @param score
     * @return
     */
    @Query("update t_score set score=:score where address in (:addresses)")
    int update(List<String> addresses, int score);

    /**
     * plus score of all addresses
     *
     * @param score
     * @return
     */
    @Query("update t_score set score=score+:plusScore")
    int plusAll(int score);
}
