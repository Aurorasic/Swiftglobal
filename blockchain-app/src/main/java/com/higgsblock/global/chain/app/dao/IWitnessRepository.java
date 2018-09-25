package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.dao.entity.WitnessEntity;
import com.higgsblock.global.chain.app.keyvalue.repository.IKeyValueRepository;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-07-12
 */
public interface IWitnessRepository extends IKeyValueRepository<WitnessEntity, Integer> {

    @Override
    @Cacheable(value = "Witness", unless = "#result == null")
    List<WitnessEntity> findAll();

}
