package com.higgsblock.global.chain.app.service;


import com.higgsblock.global.chain.app.blockchain.WitnessEntity;

import java.util.List;

/**
 * @author liuweizhen
 * @date 2018-05-21
 */
public interface IWitnessEntityService {

    /**
     * Get by height.
     *
     * @param height
     * @return
     */
    public List<WitnessEntity> getByHeight(long height);

    /**
     * Get all.
     *
     * @return
     */
    public List<WitnessEntity> getAll();
}
