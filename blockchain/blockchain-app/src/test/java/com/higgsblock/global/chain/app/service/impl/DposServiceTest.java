//package com.higgsblock.global.chain.app.service.impl;
//
//import com.alibaba.fastjson.JSONObject;
//import com.google.common.collect.Lists;
//import com.higgsblock.global.chain.app.BaseMockTest;
//import com.higgsblock.global.chain.app.dao.DposDao;
//import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
//import org.junit.Assert;
//import org.junit.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.powermock.api.mockito.PowerMockito;
//import org.rocksdb.RocksDBException;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//
///**
// * @author Su Jiulong
// * @date 2018-06-26
// */
//public class DposServiceTest extends BaseMockTest {
//
//    @Mock
//    private DposDao dposDao;
//
//    @InjectMocks
//    private DposService dposService;
//
//    @Test
//    public void get() throws RocksDBException {
//        Map<String, String> dposJsonMap = new HashMap<>(1);
//        dposJsonMap.save("dpos", "I Want to be dpos node");
//        String expected = JSONObject.toJSONString(dposJsonMap);
//        String dpos = "[" + expected + "]";
//        PowerMockito.when(dposDao.get(anyLong())).thenReturn(dpos);
//        Assert.assertEquals(expected, dposService.get(1L).get(0));
//
//        //throw RocksDBException
//        PowerMockito.when(dposDao.get(anyLong())).thenThrow(new RocksDBException("RocksDBException"));
//        try {
//            dposService.get(1L);
//        } catch (Exception e) {
//            Assert.assertTrue(e instanceof IllegalStateException);
//            Assert.assertTrue(e.getMessage().contains("Get dpos error while the sn is 1"));
//        }
//    }
//
//    @Test
//    public void save() {
//        BaseDaoEntity baseDaoEntity = PowerMockito.mock(BaseDaoEntity.class);
//        PowerMockito.when(dposDao.getEntity(anyLong(), any())).thenReturn(baseDaoEntity);
//        Assert.assertEquals(baseDaoEntity, dposService.save(1L, new ArrayList<>(1)));
//    }
//
//    @Test
//    public void keys() {
//        List<byte[]> expected = Lists.newArrayList();
//        expected.add(new byte[]{'k', 'e', 'y'});
//        PowerMockito.when(dposDao.keys()).thenReturn(expected);
//        List<byte[]> result = dposService.keys();
//        Assert.assertEquals(expected.size(), result.size());
//        for (int i = 0; i < result.get(0).length; i++) {
//            Assert.assertEquals(expected.get(0)[i], result.get(0)[i]);
//        }
//    }
//}