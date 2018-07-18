package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.SpentTransactionOutIndexEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wangxiangyi
 * @date 2018/7/13
 */
@Slf4j
public class SpentTransactionOutIndexRepositoryTest extends BaseTest {

    @Autowired
    private ISpentTransactionOutIndexRepository spentTransactionOutIndexRepository;


    @Test
    @Transactional
    public void save() {
        SpentTransactionOutIndexEntity entity = new SpentTransactionOutIndexEntity();
        entity.setNowTransactionHash("124");
        entity.setOutIndex((short) 1);
        entity.setPreTransactionHash("122");
        SpentTransactionOutIndexEntity savedEntity = spentTransactionOutIndexRepository.save(entity);
        LOGGER.info("saved SpentTransactionOutIndexEntity result : {}", savedEntity);
        Assert.assertNotNull(savedEntity);
    }

    @Test
    public void testFindByPreTransactionHash() {
        List<SpentTransactionOutIndexEntity> entities = spentTransactionOutIndexRepository.findByPreTransactionHash("122");
        LOGGER.info("find by preTransactionHash result : {}", entities);
        Assert.assertNotNull(entities);
        Assert.assertTrue(entities.size() > 0);
    }

}
