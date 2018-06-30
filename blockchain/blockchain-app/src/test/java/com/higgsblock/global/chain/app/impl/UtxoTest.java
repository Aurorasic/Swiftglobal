//package com.higgsblock.global.chain.app.impl;
//
//import com.higgsblock.global.chain.app.BaseTest;
//import com.higgsblock.global.chain.app.blockchain.Block;
//import com.higgsblock.global.chain.app.consensus.NodeManager;
//import com.higgsblock.global.chain.app.service.impl.BlockDaoService;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.rocksdb.RocksDBException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.util.SerializationUtils;
//
//import java.util.List;
//
///**
// * @author yangyi
// * @deta 2018/5/24
// * @description
// */
//
//@Slf4j
//public class UtxoTest extends BaseTest {
//
//    @Autowired
//    private UtxoDao utxoDao;
//
//    @Autowired
//    private BlockDaoService blockDaoService;
//
//    @Autowired
//    private DposDao dposDao;
//    @Autowired
//    private NodeManager nodeManager;
//
//
//    @Autowired
//    private BlockDao blockDao;
//
//    @Test
//    public void testUtxo() {
////        List<String> strings = utxoDao.allKeys();
////        System.out.println(strings);
////        List<UTXO> utxos = utxoDao.allValues();
////        utxos.forEach(utxo -> System.out.println(utxo));
//        Block bestBlockByHeight = blockDaoService.getBestBlockByHeight(2);
//        System.out.println(bestBlockByHeight);
//    }
//
//    @Test
//    public void testBlock() {
//        //blockDaoService.printAllBlockData();
//
//        blockDao.keys().stream().forEach(key -> {
//            Block b = null;
//            try {
//                String tempKey = (String) SerializationUtils.deserialize(key);
//                b = blockDao.get(tempKey);
//            } catch (RocksDBException e) {
//                e.printStackTrace();
//            }
//            LOGGER.info("Block=> {}", b);
//
//        });
//    }
//
//    @Test
//    public void testDpos(){
//        List<String> groupByHeihgt = nodeManager.getDposGroupByHeihgt(8387);
//        System.out.println(groupByHeihgt);
//    }
//}
