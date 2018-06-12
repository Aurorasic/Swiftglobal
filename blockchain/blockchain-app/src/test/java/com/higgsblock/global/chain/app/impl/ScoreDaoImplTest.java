package com.higgsblock.global.chain.app.impl;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.dao.ScoreDao;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import com.higgsblock.global.chain.app.service.impl.ScoreDaoService;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author HuangShengli
 * @date 2018-05-23
 */
public class ScoreDaoImplTest extends BaseTest {

    @Autowired
    private ScoreDaoService scoreDaoService;

    @Autowired
    private ScoreDao scoreDao;

    private void putAndGet(String key, int score) throws RocksDBException {

        BaseDaoEntity baseDaoEntity = scoreDaoService.put(key, 100);
        scoreDao.writeBatch(Arrays.asList(baseDaoEntity));
        Integer scrore = scoreDaoService.get(key);
        Assert.assertTrue(scrore == score);
    }

    private void batchPut() throws RocksDBException {
//        List<MinerScoreEntity> entities = new ArrayList<>();
//        for (int i = 101; i < 201; i++) {
//            MinerScoreEntity score = new MinerScoreEntity("address-------" + i, i);
//            entities.add(score);
//            System.out.println("put  score :" + score);
//        }

//        System.out.println("batch put scores :" + entities.size());
//        scoreDao.writeBatch(convert(entities));
    }

    private void get(String key) throws RocksDBException {
        Integer scrore = scoreDaoService.get(key);
        Assert.assertNotNull(scrore);
//        Assert.assertTrue(scrore == 10);
    }

    private void allKeysAndValues() {
        List<String> keys = scoreDao.allKeys();
        Assert.assertTrue("allKeys functions failed", !CollectionUtils.isEmpty(keys));
        keys.forEach(k -> System.out.println("key:" + k));
        List<Integer> scores = scoreDao.allValues();
        Assert.assertTrue("allValues functions failed", !CollectionUtils.isEmpty(scores));
        scores.forEach(score -> System.out.println("score:" + score));
    }

//    private void findAll() throws RocksDBException {
//        List<MinerScoreEntity> scoreEntities = scoreDaoService.findAll();
//        Assert.assertTrue(!CollectionUtils.isEmpty(scoreEntities));
//        scoreEntities.forEach(en -> System.out.println(en.getAddress() + ":" + en.getScore()));
//    }

//    private void findAllKeys() throws RocksDBException {
//        List<String> keys = scoreDaoService.findAllKeys();
//        Assert.assertTrue(!CollectionUtils.isEmpty(keys));
//        keys.forEach(en -> System.out.println(en));
//    }

    @Test
    public void test() throws RocksDBException {
//        for (int i = 1 ;i < 101;i ++){
//            putAndGet("address------" + i,i);
//        }
//        get();
//        findAll();
//        findAllKeys();
//        batchPut();
//        findAll();
        allKeysAndValues();

    }

    private List<BaseDaoEntity> convert(String address, int score) {
        List<BaseDaoEntity> list = new ArrayList<>();
        list.add(scoreDaoService.put(address, score));
        return list;
    }

//    private List<BaseDaoEntity> convert(List<MinerScoreEntity> entities) {
//        List<BaseDaoEntity> list = new ArrayList<>();
//        for (MinerScoreEntity entity : entities) {
//            list.add(scoreDaoService.put(entity.getAddress(), entity.getScore()));
//        }

//        return list;
//    }


}
