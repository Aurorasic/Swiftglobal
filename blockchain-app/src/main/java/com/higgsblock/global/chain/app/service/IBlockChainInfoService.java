package com.higgsblock.global.chain.app.service;

import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-09-06
 */
public interface IBlockChainInfoService {
    long getMaxHeight();

    void setMaxHeight(long height);

    Map<String, String> getAllScores();

    void setAllScores(Map<String, String> allScores);

    void deleteAllScores();

    void setAllWitness(String allWitnesss);
}
