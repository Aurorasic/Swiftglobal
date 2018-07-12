package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.BlockEntity;
import com.higgsblock.global.chain.app.dao.iface.IBlockRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
@Slf4j
public class BlockRepositoryTest extends BaseTest {

    @Autowired
    private IBlockRepository blockRepository;

    @Test
    public void testFindAll() {
        blockRepository.findAll();
    }

    @Test
    public void testGetOne() {
    }

    @Test
    @Transactional
    public void testSave() {
        BlockEntity blockEntity = new BlockEntity();
        blockEntity.setBlockHash("123");
        blockEntity.setHeight(11);
        blockEntity.setData("data-test");
        BlockEntity result = blockRepository.save(blockEntity);
        LOGGER.info("save result: {}", result);

        BlockEntity findByIdResult = blockRepository.findOne(1L);
        LOGGER.info("find one by id result : {}", findByIdResult);
    }

    @Test
    @Transactional
    public void testFindByExample() {
        BlockEntity blockEntity = new BlockEntity();
        blockEntity.setBlockHash("123");
        blockEntity.setHeight(11);
        blockEntity.setData("data-test");
        BlockEntity result = blockRepository.save(blockEntity);
        LOGGER.info("save result: {}", result);

        BlockEntity queryEntity = new BlockEntity();
        queryEntity.setBlockHash("123");
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("data", "height")
                .withMatcher("blockHash", ExampleMatcher.GenericPropertyMatchers.contains());
        Example<BlockEntity> example = Example.of(queryEntity, matcher);
        List<BlockEntity> resultEntities = blockRepository.findAll(example);
        LOGGER.info("find one by example result : {}", resultEntities);
    }

    @Test
    public void testQueryByBlockHash() {
        BlockEntity entity = blockRepository.queryByBlockHash("123");
        LOGGER.info("query by blockHash result: {}", entity);
    }

}
