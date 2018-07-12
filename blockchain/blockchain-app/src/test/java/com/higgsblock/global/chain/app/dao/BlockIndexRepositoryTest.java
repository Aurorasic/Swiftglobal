package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.BlockIndexEntity;
import com.higgsblock.global.chain.app.dao.iface.IBlockIndexRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wangxiangyi
 * @date 2018/7/12
 */
@Slf4j
public class BlockIndexRepositoryTest extends BaseTest {

    @Autowired
    private IBlockIndexRepository blockIndexRepository;

    @Test
    @Transactional
    public void testSave() {
        BlockIndexEntity indexEntity = new BlockIndexEntity();
        indexEntity.setBlockHash("124");
        indexEntity.setHeight(12);
        indexEntity.setIsBest(1);
        indexEntity.setMinerAddress("miner address test2");
        BlockIndexEntity result = blockIndexRepository.save(indexEntity);
        LOGGER.info("--->>save result : {}", result);
    }

    @Test
    public void testQueryByBlockHash() {
        BlockIndexEntity indexEntity = blockIndexRepository.queryByBlockHash("123");
        LOGGER.info("--->>find one result : {}", indexEntity);
    }

    @Test
    @Transactional
    public void testUpdate() {
        BlockIndexEntity indexEntity = blockIndexRepository.queryByBlockHash("123");
        indexEntity.setIsBest(0);
        BlockIndexEntity savedEntity = blockIndexRepository.save(indexEntity);
        LOGGER.info("--->>save or update ? save by id ：{}", savedEntity);
    }

    @Test
    public void testQueryAllByHeight() {
        List<BlockIndexEntity> indexEntities = blockIndexRepository.queryAllByHeight(12);
        LOGGER.info("query all by height size : {}", indexEntities.size());
    }

    @Test
    public void testQueryMaxHeight() {
        long maxHeight = blockIndexRepository.queryMaxHeight();
        LOGGER.info("query max height result : {}", maxHeight);
    }

}
