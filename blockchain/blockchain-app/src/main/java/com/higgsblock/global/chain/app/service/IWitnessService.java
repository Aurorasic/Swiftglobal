package com.higgsblock.global.chain.app.service;


import com.higgsblock.global.chain.network.Peer;

import java.util.List;

/**
 * @author liuweizhen
 * @date 2018-05-21
 */
public interface IWitnessService {

    /**
     * Get all.
     *
     * @return
     */
    List<Peer> getAllWitnessPeer();

    boolean isWitness(String address);

    int getWitnessSize();
}
