package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.BlockIndexEntity;
import com.higgsblock.global.chain.app.dao.iface.BlockIndexRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
@Slf4j
public class BlockIndexRepositoryTest extends BaseTest {

    @Autowired
    private BlockIndexRepository blockIndexRepository;

    @Test
    public void testFindOne() {
        /*BlockIndexEntity indexEntity = blockIndexRepository.findOne("614c2de5d11d90acfe86fda9e6e10df90c9904973b82123955431647e735c63e");
        LOGGER.info("find one result : {}", indexEntity);*/
    }
}
