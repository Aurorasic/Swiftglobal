package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.WitnessEntity;
import com.higgsblock.global.chain.app.dao.iface.IWitnessRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-07-12
 */
public class IWitnessRepositoryTest extends BaseTest {

    @Autowired
    private IWitnessRepository iWitnessRepository;

    @Test
    public void findAll() {
        List<WitnessEntity> all = iWitnessRepository.findAll();
        for (WitnessEntity witnessEntity : all) {
            System.err.println(witnessEntity);
        }
    }
}
