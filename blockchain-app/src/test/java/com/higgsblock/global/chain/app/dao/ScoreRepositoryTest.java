package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.Transactional;
import com.higgsblock.global.chain.app.service.IDposService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;

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
    @Autowired
    private IDposService dposService;

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
        ScoreEntity savedEntity = scoreRepository.save(scoreEntity);
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

    public void testQueryTestLimit() {
        Pageable pageable = new PageRequest(0, 1000, Sort.Direction.DESC, "score", "address");
        String[] addresses = {"1BdgnGcgBhw4LRaictfF4nxGKo228BQNqW", "1234fMcU3YJUCGsfy61DQFdciUzLG4qyeR"};
//        Arrays.asList(addresses);
        List<ScoreEntity> scoreEntities = scoreRepository.queryTopScoreByRange(800, 1000, new ArrayList<>(), pageable);
        if (CollectionUtils.isNotEmpty(scoreEntities)) {
            scoreEntities.forEach(score -> System.out.println(score));
        } else {
            System.out.println("query result is empty");
        }
    }

    public void testSelectNextDpos() {
        Block toBeBlock = new Block();
        toBeBlock.setHeight(207);
        toBeBlock.setHash("545e64a48860ea05659c55cd3313d0a9dfe17d4c310a9e0f28e19384e5d3c6b8");
        dposService.calcNextDposNodes(toBeBlock, 210);
    }

    @Test
    public void test() {
        testQueryTestLimit();
//        testSelectNextDpos();
    }


}
