package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.dao.IWitnessRepository;
import com.higgsblock.global.chain.app.dao.entity.WitnessEntity;
import com.higgsblock.global.chain.app.service.IWitnessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The type Witness service.
 *
 * @author liuweizhen
 * @date 2018 -05-21
 */
@Service
public class WitnessService implements IWitnessService {

    /**
     * The Witness repository.
     */
    @Autowired
    private IWitnessRepository witnessRepository;

    @Override
    public List<WitnessEntity> getAll() {
        return witnessRepository.findAll();
    }
}