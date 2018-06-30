package com.higgsblock.global.chain.app.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.higgsblock.global.chain.app.blockchain.WitnessEntity;
import com.higgsblock.global.chain.app.dao.entity.WitnessPo;
import com.higgsblock.global.chain.app.dao.iface.IWitnessEntity;
import com.higgsblock.global.chain.app.service.IWitnessEntityService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author liuweizhen
 * @date 2018-05-21
 */
@Service
public class WitnessEntityService implements IWitnessEntityService {

    /**
     * TODO lwz pre mine block num 2018-05-26
     */
    private static final int PRE_BLOCKS = 13;
    /**
     * TODO lwz block num for a round of witness to sign 2018-05-26
     */
    private static final int BATCH = 200;

    private static final Cache<String, List<WitnessEntity>> WITNESS_CACHE = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .build();


    @Autowired
    private IWitnessEntity iWitnessEntity;

    @Override
    public List<WitnessEntity> getByHeight(long height) {
        String key = buildCacheKey(height);

        List<WitnessEntity> list = WITNESS_CACHE.getIfPresent(key);
        if (list == null) {
            List<WitnessPo> byHeight = iWitnessEntity.getByHeight(height);
            for (WitnessPo witnessPo : byHeight) {
                WitnessEntity witnessEntity = new WitnessEntity();
                BeanUtils.copyProperties(witnessPo, witnessEntity);
                list.add(witnessEntity);
            }
            if (list != null) {
                WITNESS_CACHE.put(key, list);
            }
        }

        return list;
    }

    @Override
    public List<WitnessEntity> getAll() {
        List<WitnessPo> witnessPos = iWitnessEntity.findAll();
        List<WitnessEntity> witnessEntities = new ArrayList<>();
        for (WitnessPo witnessPo : witnessPos) {
            WitnessEntity witnessEntity = new WitnessEntity();
            BeanUtils.copyProperties(witnessPo, witnessEntity);
            witnessEntities.add(witnessEntity);
        }
        return witnessEntities;
    }

    private String buildCacheKey(long height) {
        Preconditions.checkArgument(height > PRE_BLOCKS, String.format("too samll height to find witness, %s", height));

        long start = PRE_BLOCKS + 1;
        long end = start + BATCH - 1;

        while (height > end) {
            start = end + 1;
            end = start + BATCH - 1;
        }


        return start + "_" + end;
    }
}
