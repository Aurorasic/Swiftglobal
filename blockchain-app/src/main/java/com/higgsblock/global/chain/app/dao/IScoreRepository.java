package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.IndexQuery;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
public interface IScoreRepository extends IKeyValueRepository<ScoreEntity, Long> {

    /**
     * find ScoreEntity by address
     *
     * @param address
     * @return
     * @author wangxiangyi
     * @date 2018/7/13
     */
    @IndexQuery("address")
    ScoreEntity findByAddress(String address);

    /**
     * find all score address order by score and address
     *
     * @return
     */
    List<ScoreEntity> findAllOrderByScoreAndAddress();

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
    @Deprecated
    int updateByAddress(@Param("addresses") List<String> addresses, @Param("updateScore") int score);

    /**
     * plus score of all addresses
     *
     * @param score
     * @return
     */
    @Deprecated
    int plusAll(@Param("plusScore") int score);

    /**
     * query top score by range
     *
     * @param minScore
     * @param maxScore
     * @param addressList
     * @param pageable
     * @return
     */
    @Deprecated
    List<ScoreEntity> queryTopScoreByRange(@Param("minScore") Integer minScore, @Param("maxScore") Integer maxScore,
                                           @Param("addressList") List<String> addressList, Pageable pageable);

}
