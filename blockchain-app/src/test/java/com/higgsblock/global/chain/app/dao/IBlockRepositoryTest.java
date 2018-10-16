package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.service.impl.UTXOServiceProxy;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author tangkun
 * @date 2018-10-16
 */
@Slf4j
public class IBlockRepositoryTest extends BaseTest {

    @Autowired
    private IBlockRepository blockRepository;

    @Autowired
    private UTXOServiceProxy utxoServiceProxy;

    @Autowired
    private IContractRepository contractRepository;

    @Test
    public void testFindByHeight() throws Exception {
        LOGGER.info("block:{}", blockRepository.findByHeight(2L));
    }

    @Test
    public void testKey() {
        LOGGER.info("key:{}", contractRepository.findAll());
    }

}