package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.WitnessEntity;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yangshenghong
 * @date 2018-07-12
 */
public class IWitnessRepositoryTest extends BaseTest {

    @Autowired
    private IWitnessRepository witnessRepository;

    @Test
    public void findAll() {
        for (WitnessEntity witnessEntity : witnessRepository.findAll()) {
            System.err.println(witnessEntity);
        }
    }
}
