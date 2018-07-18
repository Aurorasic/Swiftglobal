package com.higgsblock.global.chain.app.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.higgsblock.global.chain.app.dao.IWitnessRepository;
import com.higgsblock.global.chain.app.dao.entity.WitnessEntity;
import com.higgsblock.global.chain.app.service.IWitnessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The type Witness service.
 *
 * @author liuweizhen
 * @date 2018 -05-21
 */
@Service
public class WitnessService implements IWitnessService {
    /**
     * The constant WITNESS_CACHE.
     */
    private static final Cache<String, List<com.higgsblock.global.chain.app.blockchain.WitnessEntity>> WITNESS_CACHE = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();


    /**
     * The Witness repository.
     */
    @Autowired
    private IWitnessRepository iWitnessRepository;

    @Override
    public List<WitnessEntity> getAll() {
        return iWitnessRepository.findAll();
    }
}