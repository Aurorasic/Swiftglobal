package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.dao.entity.BlockChainInfoEntity;
import com.higgsblock.global.chain.app.net.peer.Peer;

import java.util.List;
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

    List<Peer> getAllWitness();

    void setAllWitness(String allWitnesss);
}
