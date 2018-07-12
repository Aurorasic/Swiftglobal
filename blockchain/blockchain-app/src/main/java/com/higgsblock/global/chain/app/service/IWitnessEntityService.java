package com.higgsblock.global.chain.app.service;


import com.higgsblock.global.chain.app.dao.entity.WitnessEntity;

import java.util.List;

/**
 * @author liuweizhen
 * @date 2018-05-21
 */
public interface IWitnessEntityService {

    /**
     * Get all.
     *
     * @return
     */
    List<WitnessEntity> getAll();
}
