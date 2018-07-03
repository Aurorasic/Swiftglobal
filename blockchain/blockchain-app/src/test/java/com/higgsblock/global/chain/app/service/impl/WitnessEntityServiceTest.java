//package com.higgsblock.global.chain.app.service.impl;
//
//import com.higgsblock.global.chain.app.BaseMockTest;
//import com.higgsblock.global.chain.app.blockchain.WitnessEntity;
//import com.higgsblock.global.chain.app.dao.WitnessEntityDao;
//import org.junit.Assert;
//import org.junit.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.powermock.api.mockito.PowerMockito;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author yangshenghong
// * @date 2018-06-27
// */
//public class WitnessEntityServiceTest extends BaseMockTest {
//
//    @Mock
//    private WitnessEntityDao witnessEntityDao;
//
//    @InjectMocks
//    private WitnessEntityService witnessEntityService;
//
//    @Test
//    public void getByHeight() throws Exception {
//        long height = 100;
//        List<WitnessEntity> witnessEntities = new ArrayList<>();
//        PowerMockito.when(witnessEntityDao.getByHeight(height)).thenReturn(witnessEntities);
//        Assert.assertEquals(witnessEntities, witnessEntityService.getByHeight(height));
//        long height2 = 215;
//        List<WitnessEntity> witnessEntities2 = new ArrayList<>();
//        PowerMockito.when(witnessEntityDao.getByHeight(height2)).thenReturn(witnessEntities2);
//        Assert.assertEquals(witnessEntities2, witnessEntityService.getByHeight(height2));
//    }
//
//    @Test
//    public void addAll() throws Exception {
//        List<WitnessEntity> entities = new ArrayList<>();
//        PowerMockito.when(witnessEntityDao.addAll(entities)).thenReturn(true);
//        Assert.assertTrue(witnessEntityService.addAll(entities));
//        PowerMockito.when(witnessEntityDao.addAll(entities)).thenReturn(false);
//        Assert.assertFalse(witnessEntityService.addAll(entities));
//
//    }
//
//    @Test
//    public void getAll() throws Exception {
//        List<WitnessEntity> witnessEntities = new ArrayList<>();
//        PowerMockito.when(witnessEntityDao.getAll()).thenReturn(witnessEntities);
//        Assert.assertEquals(witnessEntities.size(), witnessEntityService.getAll().size());
//    }
//
//}