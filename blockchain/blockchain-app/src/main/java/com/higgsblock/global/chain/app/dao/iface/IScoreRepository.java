package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Query("update ScoreEntity ts set ts.score=:updateScore where ts.address in :addresses")
    @Modifying
    int updateByAddress(@Param("addresses") List<String> addresses, @Param("updateScore") int score);

    /**
     * plus score of all addresses
     *
     * @param score
     * @return
     */
    @Query("update ScoreEntity set score=score + :plusScore")
    @Modifying
    int plusAll(@Param("plusScore") int score);
}
