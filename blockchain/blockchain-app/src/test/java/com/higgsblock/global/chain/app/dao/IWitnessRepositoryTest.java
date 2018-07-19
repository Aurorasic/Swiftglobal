package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.WitnessEntity;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-07-12
 */
public class IWitnessRepositoryTest extends BaseTest {

    @Autowired
    private IWitnessRepository witnessRepository;

    @Test
    public void findAll() {
        List<WitnessEntity> all = witnessRepository.findAll();
        for (WitnessEntity witnessEntity : all) {
            System.err.println(witnessEntity);
        }
    }
}
