package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.DposEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
@Slf4j
public class DposRepositoryTest extends BaseTest {

    @Autowired
    private IDposRepository dposRepository;

    @Test
    @Transactional
    public void testSave() {
        DposEntity result = dposRepository.save(new DposEntity( 13, "dpos address test"));
        LOGGER.info("--->>save result: {}", result);
    }

    @Test
    public void testQueryBySn() {
        DposEntity entity = dposRepository.findBySn(13);
        LOGGER.info("--->>query by sn result : {}", entity);
    }

}
