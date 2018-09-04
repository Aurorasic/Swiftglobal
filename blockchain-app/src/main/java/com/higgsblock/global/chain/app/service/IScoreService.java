package com.higgsblock.global.chain.app.service;


import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.common.ScoreRangeEnum;
import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;

import java.util.List;
import java.util.Map;

/**
 * @author HuangShengli
 * @date 2018-05-23
 */
public interface IScoreService {

    /**
     * init score for new miner
     */
    int INIT_SCORE = 0;

    /**
     * set init score for dpos miner
     */
    int SELECTED_DPOS_SET_SCORE = 600;
    /**
     * set score for miner who mined the block
     */
    int MINED_BLOCK_SET_SCORE = 800;
    /**
     * set score for offline miner(mined block by guarder)
     */
    int OFFLINE_MINER_SET_SCORE = 0;
    /**
     * plus score
     */
    int ONE_BLOCK_ADD_SCORE = 1;

    /**
     * max limit record
     */
    int SCORE_LIMIT_NUM = 1000;
    /**
     * order by field
     */
    String SCORE_ORDERBY_FIELD = "score";
    /**
     * order by field
     */
    String ADDRESS_ORDERBY_FIELD = "address";

    /**
     * get score by address
     *
     * @param address
     * @return
     */
    Integer get(String address);

    /**
     * set score
     *
     * @param address
     * @param score
     * @return
     */
    void put(String address, Integer score);

    /**
     * batch update
     *
     * @param addressList
     * @param score
     * @return
     */
    int updateBatch(List<String> addressList, int score);

    /**
     * Score all the records
     * add or sub score
     *
     * @param score
     * @return
     */
    int plusAll(Integer score);

    /**
     * set score if not exist
     *
     * @param address
     * @param score
     * @return
     */
    void putIfAbsent(String address, Integer score);

    /**
     * remove score
     *
     * @param address
     * @return
     */
    void remove(String address);

    /**
     * load all score
     *
     * @return
     */
    Map<String, Integer> loadAll();

    /**
     * load all score of miners
     *
     * @return
     */
    List<ScoreEntity> all();

    /**
     * set score for dpos miners
     *
     * @param addressList
     */
    void setSelectedDposScore(List<String> addressList);

    /**
     * refresh score for miner who produced the best block
     *
     * @param toBeBestBlock
     */
    void refreshMinersScore(Block toBeBestBlock);

    /**
     * list top 1000 by score range
     *
     * @param scoreRange
     * @param exculdeAddresses
     * @return
     */
    List<String> queryAddresses(ScoreRangeEnum scoreRange, List<String> exculdeAddresses);

}
