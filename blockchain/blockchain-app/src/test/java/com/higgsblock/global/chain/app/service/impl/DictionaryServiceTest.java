//package com.higgsblock.global.chain.app.service.impl;
//
//import com.higgsblock.global.chain.app.BaseMockTest;
//import com.higgsblock.global.chain.app.blockchain.LatestBestBlockIndex;
//import com.higgsblock.global.chain.app.dao.LatestBlockIndexDao;
//import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
//import org.junit.Assert;
//import org.junit.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//
//import static org.mockito.ArgumentMatchers.anyString;
//
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.rocksdb.RocksDBException;
//
///**
// * @author Su Jiulong
// * @date 2018-06-26
// */
//@PrepareForTest({DictionaryService.class})
//public class DictionaryServiceTest extends BaseMockTest {
//
//    private static final String LATEST_BEST_BLOCK_INDEX = "latest-best-block-index";
//
//    @Mock
//    private LatestBlockIndexDao latestBlockIndexDao;
//
//    @InjectMocks
//    private DictionaryService dictionaryService;
//
//    @Test
//    public void saveLatestBestBlockIndex() throws Exception {
//        BaseDaoEntity baseDaoEntity = PowerMockito.mock(BaseDaoEntity.class);
//        long height = 123L;
//        String bestBlockHash = "bestBlockHash";
//        LatestBestBlockIndex index = PowerMockito.spy(new LatestBestBlockIndex());
//        index.setHeight(height);
//        index.setBestBlockHash(bestBlockHash);
//        PowerMockito.whenNew(LatestBestBlockIndex.class).withAnyArguments().thenReturn(index);
//        PowerMockito.when(latestBlockIndexDao.getEntity(LATEST_BEST_BLOCK_INDEX, index)).thenReturn(baseDaoEntity);
//        Assert.assertEquals(baseDaoEntity, dictionaryService.saveLatestBestBlockIndex(height, bestBlockHash));
//    }
//
//    @Test
//    public void getLatestBestBlockIndex() throws RocksDBException {
//        LatestBestBlockIndex index = new LatestBestBlockIndex(123L, "bestBlockHash");
//        //do not throw RocksDBException
//        PowerMockito.when(latestBlockIndexDao.get(anyString())).thenReturn(index);
//        LatestBestBlockIndex result = dictionaryService.getLatestBestBlockIndex();
//        Assert.assertEquals(123L, result.getHeight());
//        Assert.assertEquals("bestBlockHash", result.getBestBlockHash());
//
//        //throw RocksDBException
//        PowerMockito.when(latestBlockIndexDao.get(anyString())).thenThrow(new RocksDBException("RocksDBException"));
//        try {
//            dictionaryService.getLatestBestBlockIndex();
//        } catch (Exception e) {
//            Assert.assertTrue(e instanceof IllegalStateException);
//            Assert.assertTrue(e.getMessage().contains("Get latest best block error"));
//        }
//    }
//}