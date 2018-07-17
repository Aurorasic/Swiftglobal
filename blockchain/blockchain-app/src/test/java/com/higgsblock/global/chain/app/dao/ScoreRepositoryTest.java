package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;
import com.higgsblock.global.chain.app.dao.iface.IScoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangxiangyi
 * @date 2018/7/13
 */
@Slf4j
public class ScoreRepositoryTest extends BaseTest {

    @Autowired
    private IScoreRepository scoreRepository;

    @Test
    @Transactional
    public void testSave() {
        ScoreEntity scoreEntity = new ScoreEntity("123", 20);
        ScoreEntity savedEntity = scoreRepository.save(scoreEntity);
        LOGGER.info("saved ScoreEntity: {}", savedEntity);
        Assert.assertNotNull(savedEntity);
    }

    @Test
    public void testFindByAddress() {
        ScoreEntity scoreEntity = scoreRepository.findByAddress("123");
        LOGGER.info("find by address result : {}", scoreEntity);
        Assert.assertNotNull(scoreEntity);
    }

    @Test
    @Transactional
    public void testDeleteByAddress() {
        int rows = scoreRepository.deleteByAddress("123");
        LOGGER.info("delete by address result rows : {}", rows);
        ScoreEntity scoreEntity = scoreRepository.findByAddress("123");
        Assert.assertNull(scoreEntity);
    }

    @Test
    public void testSaveAndFlush() {
        ScoreEntity scoreEntity = scoreRepository.findByAddress("address1");
        LOGGER.info("--->>find by address result : {}", scoreEntity);

        scoreEntity.setScore(22);
        ScoreEntity savedEntity = scoreRepository.saveAndFlush(scoreEntity);
        LOGGER.info("--->>saved ScoreEntity : {}", savedEntity);
    }

    @Test
    @Transactional
    @Rollback(false)
    public void testUpdate() {
        List<String> strings = new ArrayList<>();
        strings.add("address1");
        strings.add("address2");
        strings.add("address3");
        scoreRepository.updateByAddress(strings, 10);
    }

    @Test
    @Transactional
    @Rollback(false)
    public void testPlusAll() {
        scoreRepository.plusAll(120);
    }

}
