package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.BlockEntity;
import com.higgsblock.global.chain.app.dao.iface.BlockRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
@Slf4j
public class BlockRepositoryTest extends BaseTest {

    @Autowired
    private BlockRepository blockRepository;

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

    }

}
