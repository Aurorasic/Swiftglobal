//package com.higgsblock.global.chain.app.service.impl;
//
//import com.higgsblock.global.chain.app.BaseMockTest;
//import com.higgsblock.global.chain.app.dao.ScoreDao;
//import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
//import org.junit.Assert;
//import org.junit.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.powermock.api.mockito.PowerMockito;
//import org.rocksdb.RocksDBException;
//import org.rocksdb.RocksIterator;
//import org.springframework.util.SerializationUtils;
//
//import java.util.Map;
//
///**
// * @author Su Jiulong
// * @date 2018-06-25
// */
//public class ScoreDaoServiceTest extends BaseMockTest {
//
//    @Mock
//    private ScoreDao scoreDao;
//
//    @InjectMocks
//    private ScoreDaoService scoreDaoService;
//
//    @Test
//    public void get() throws RocksDBException {
//        String address = "address";
//        PowerMockito.when(scoreDao.get(address)).thenReturn(1);
//        Assert.assertEquals((Integer) 1, scoreDaoService.get(address));
//    }
//
//    @Test
//    public void save() {
//        String address = "address";
//        int score = 123;
//        BaseDaoEntity baseDaoEntity = new BaseDaoEntity(address, score, "score");
//        PowerMockito.when(scoreDao.getEntity(address, score)).thenReturn(baseDaoEntity);
//        BaseDaoEntity result = scoreDaoService.save(address, score);
//        Assert.assertEquals("address", result.getKey());
//        Assert.assertEquals(123, result.getValue());
//        Assert.assertEquals("score", result.getColumnFamilyName());
//    }
//
//    @Test
//    public void putIfAbsent1() throws RocksDBException {
//        String address = "address";
//        Integer localScore = 111;
//        BaseDaoEntity baseDaoEntity = new BaseDaoEntity(address, localScore, "score");
//        PowerMockito.when(scoreDao.getEntity(address, localScore)).thenReturn(baseDaoEntity);
//        //the address has localScore
//        PowerMockito.when(scoreDao.get(address)).thenReturn(localScore);
//        BaseDaoEntity result = scoreDaoService.putIfAbsent(address, localScore);
//        Assert.assertEquals("address", result.getKey());
//        Assert.assertEquals(111, result.getValue());
//        Assert.assertEquals("score", result.getColumnFamilyName());
//
//    }
//
//    @Test
//    public void putIfAbsent2() throws RocksDBException {
//        String address = "address";
//        //the address has no localScore
//        int score = 123;
//        PowerMockito.when(scoreDao.get(address)).thenReturn(null);
//        BaseDaoEntity baseDaoEntity1 = new BaseDaoEntity(address, score, "score");
//        PowerMockito.when(scoreDao.getEntity(address, score)).thenReturn(baseDaoEntity1);
//        BaseDaoEntity result1 = scoreDaoService.putIfAbsent(address, score);
//        Assert.assertEquals("address", result1.getKey());
//        Assert.assertEquals(123, result1.getValue());
//        Assert.assertEquals("score", result1.getColumnFamilyName());
//    }
//
//    @Test
//    public void remove() {
//        String address = "address";
//        BaseDaoEntity baseDaoEntity = new BaseDaoEntity(address, null, "score");
//        PowerMockito.when(scoreDao.getEntity(address, null)).thenReturn(baseDaoEntity);
//        BaseDaoEntity result = scoreDaoService.remove(address);
//        Assert.assertEquals("address", result.getKey());
//        Assert.assertEquals(null, result.getValue());
//        Assert.assertEquals("score", result.getColumnFamilyName());
//    }
//
//    @Test
//    public void loadAll() throws RocksDBException {
//        RocksIterator iterator = PowerMockito.mock(RocksIterator.class);
//        PowerMockito.when(scoreDao.iterator()).thenReturn(iterator);
//        PowerMockito.when(iterator.isOwningHandle()).thenReturn(true);
//        PowerMockito.when(iterator.isValid()).thenReturn(true).thenReturn(false);
//        PowerMockito.when(iterator.key()).thenReturn(SerializationUtils.serialize("address"));
//        PowerMockito.when(iterator.value()).thenReturn(SerializationUtils.serialize("111"));
//
//        Map<String, Integer> scoreMap = scoreDaoService.loadAll();
//        for (Map.Entry<String, Integer> entry : scoreMap.entrySet()) {
//            Assert.assertEquals("address", entry.getKey());
//            Assert.assertEquals((Integer) 111, entry.getValue());
//        }
//
//        PowerMockito.when(iterator.isValid()).thenReturn(true).thenReturn(false);
//        //throw NumberFormatException
//        PowerMockito.when(iterator.key()).thenReturn(SerializationUtils.serialize("address"));
//        PowerMockito.when(iterator.value()).thenReturn(SerializationUtils.serialize("111.0"));
//        try {
//            scoreDaoService.loadAll();
//        } catch (Exception e) {
//            Assert.assertTrue(e instanceof NumberFormatException);
//            Assert.assertTrue(e.getMessage().contains("score data format error from db"));
//        }
//    }
//}