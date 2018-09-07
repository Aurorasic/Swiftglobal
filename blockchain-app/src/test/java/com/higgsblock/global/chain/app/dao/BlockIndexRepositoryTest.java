package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.entity.BlockIndexEntity;
import com.higgsblock.global.chain.app.keyvalue.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        BlockIndexEntity indexEntity = blockIndexRepository.findByBlockHash("9640862a377237a6e6a9bf91cf9c500071e694ca9fa061a26bcc2a3d924d00f4");
        LOGGER.info("--->>find one result : {}", indexEntity);
    }

    @Test
    @Transactional
    public void testUpdate() {
        BlockIndexEntity indexEntity = blockIndexRepository.findByBlockHash("123");
        indexEntity.setIsBest(0);
        BlockIndexEntity savedEntity = blockIndexRepository.save(indexEntity);
        LOGGER.info("--->>save or update ? save by id ：{}", savedEntity);
    }

    @Test
    public void testQueryAllByHeight() {
        List<BlockIndexEntity> indexEntities = blockIndexRepository.findByHeight(12);
        LOGGER.info("query all by height size : {}", indexEntities.size());
    }

}
