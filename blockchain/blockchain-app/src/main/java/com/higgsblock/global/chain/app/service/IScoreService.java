package com.higgsblock.global.chain.app.service;


import java.util.List;
import java.util.Map;

/**
 * @author HuangShengli
 * @date 2018-05-23
 */
public interface IScoreService {
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
     * @param addressList
     * @param score
     * @return
     */
    int updateBatch(List<String> addressList, int score);

    /**
     * add or sub score
     *
     * @param score
     */
    int updateAll(Integer score);

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

}
