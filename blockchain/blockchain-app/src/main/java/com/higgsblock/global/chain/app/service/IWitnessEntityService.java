package com.higgsblock.global.chain.app.service;


import com.higgsblock.global.chain.app.dao.entity.WitnessPo;

import java.util.List;

/**
 * @author liuweizhen
 * @date 2018-05-21
 */
public interface IWitnessEntityService {

   /* *//**
     * Get by height.
     *
     * @param height
     * @return
     *//*
    List<WitnessEntity> getByHeight(long height);*/


    /**
     * Get all.
     *
     * @return
     */
    List<WitnessPo> getAll();
}
