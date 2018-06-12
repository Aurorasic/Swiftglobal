package com.higgsblock.global.chain.app.impl;

import com.higgsblock.global.chain.app.BaseTest;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionIndex;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.dao.*;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import com.higgsblock.global.chain.app.service.IScoreService;
import com.higgsblock.global.chain.app.service.impl.BlockDaoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author HuangShengli
 * @date 2018-05-23
 */
@Slf4j
public class BlockDaoImplTest extends BaseTest {

    @Autowired
    private BlockDaoService blockDaoService;

    @Autowired
    private BlockDao blockDao;
    @Autowired
    private BlockIndexDao blockIndexDao;
    @Autowired
    private TransDao transDao;

    @Autowired
    private UtxoDao utxoDao;

    @Autowired
    private ScoreDao scoreDao;
    @Autowired
    private IScoreService scoreDaoService;
//    @Autowired
//    private WitnessDao witnessDao;

    @Autowired
    private WitnessBlockDao witnessBlockDao;


    private void findAllBlock(List<BaseDaoEntity> all) {
        LOGGER.info("query all block...");
        List<Block> values = blockDao.allValues();
        if (CollectionUtils.isEmpty(values)) {
            LOGGER.error("block is empty!");
            return;
        }
        values.forEach(v -> {
            LOGGER.info("find block:{}", v);
            all.add(blockDao.getEntity(v.getHash(), v));
        });

    }

    private void findAllBlockIndex(List<BaseDaoEntity> all) {
        LOGGER.info("query all blockindex...");
        List<BlockIndex> values = blockIndexDao.allValues();
        if (CollectionUtils.isEmpty(values)) {
            LOGGER.error("blockindex is empty!");
        }
        values.forEach(v -> {
            LOGGER.info("find blockindex:{}", v);
            all.add(blockIndexDao.getEntity(v.getHeight(), v));
        });

    }

    private void findAllTransIndex(List<BaseDaoEntity> all) {
        LOGGER.info("query all transaction Index...");
        List<TransactionIndex> values = transDao.allValues();
        if (CollectionUtils.isEmpty(values)) {
            LOGGER.warn("transaction Index is empty!");
        }
//        List<String> keys = transDao.allKeys();
//        keys.forEach(key -> {
//            try {
//                TransactionIndex transactionIndex = transDao.get(key);
//                LOGGER.info("transIndex,key:{},value:{}", key, transactionIndex);
//                all.add(transDao.getEntity(key, transactionIndex));
//            } catch (RocksDBException e) {
//                e.printStackTrace();
//            }
//        });
        values.forEach(v -> {
            LOGGER.info("find transaction Index:{}", v);
            all.add(transDao.getEntity(v.getTxHash(), v));
        });

    }

    private void findAllUtxo(List<BaseDaoEntity> all) {
        LOGGER.info("query all UTXO...");
        List<UTXO> values = utxoDao.allValues();
        if (CollectionUtils.isEmpty(values)) {
            LOGGER.warn("UTXO is empty!!!");
        }
        List<String> keys = utxoDao.allKeys();
        keys.forEach(key -> {
            try {
                LOGGER.info("utxo,key:{},value:{}", key, utxoDao.get(key));
            } catch (RocksDBException e) {
                e.printStackTrace();
            }
        });
        values.forEach(v -> {
            LOGGER.info("find UTXO:{}", v);
            all.add(utxoDao.getEntity(v.getHash() + "_" + v.getIndex(), v));
        });
    }

    private void findAllScore(List<BaseDaoEntity> all) throws RocksDBException {
        LOGGER.info("query all score by DAO...");
        List<String> keys = scoreDao.allKeys();
        if (CollectionUtils.isEmpty(keys)) {
            LOGGER.warn("score is empty!!!");
        }
        for (String key : keys) {
            Integer scoreMap = scoreDao.get(key);
            LOGGER.info("find score:{}==>{}", key, scoreMap);
            all.add(scoreDao.getEntity(key, scoreMap));
        }
        LOGGER.info("query all score by DaoService...");

        Map<String, Integer> allScore = scoreDaoService.loadAll();
        for (Iterator<Map.Entry<String, Integer>> iterator = allScore.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Integer> entry = iterator.next();
            LOGGER.info("score key:{},score:{}", entry.getKey(), entry.getValue());
        }
    }

    private void fakeScore() throws RocksDBException {
        List<UTXO> values = utxoDao.allValues();
        if (CollectionUtils.isEmpty(values)) {
            LOGGER.warn("UTXO is empty!!!");
            return;
        }
        Map<String, BaseDaoEntity> entities = new HashMap<>();
        values.forEach(v -> entities.put(v.getAddress(), scoreDao.getEntity(v.getAddress(), (int) (Math.random() * 100))));
        List<BaseDaoEntity> writeList = new ArrayList<>();
        writeList.addAll(entities.values());
        writeList.forEach(en -> LOGGER.info("write score :{}", en));
        scoreDao.writeBatch(writeList);

        List<String> keys = scoreDao.allKeys();
        keys.forEach(key -> {
            try {
                LOGGER.info("query db,key:{},value:{}", key, scoreDao.get(key));
            } catch (RocksDBException e) {
                LOGGER.error("db error", e);
            }
        });
    }

//    private void findAllWitness(List<BaseDaoEntity> all) {
//        LOGGER.info("query all witness...");
//        List<WitnessEntity> values = witnessDao.allValues();
//        if (CollectionUtils.isEmpty(values)) {
//            LOGGER.warn("witness is empty!!!");
//        }
//        values.forEach(v -> {
//            LOGGER.info("find witness:{}", v);
//            all.add(witnessDao.getEntity(v.getPubKey(), v));
//        });
//    }

    private void findAllWitnessBlock(List<BaseDaoEntity> all) {
        LOGGER.info("query all witness block...");
        List<Block> values = witnessBlockDao.allValues();
        if (CollectionUtils.isEmpty(values)) {
            LOGGER.warn("witness block is empty!!!");
        }
        values.forEach(v -> {
            LOGGER.info("find witness block:{}", v);
            all.add(witnessBlockDao.getEntity(v.getHeight(), v));
        });
    }


    @Test
    public void test() throws RocksDBException {
        List<BaseDaoEntity> all = new ArrayList<>();
        //1.block
        findAllBlock(all);
        //2.blockIndex
        findAllBlockIndex(all);
        //3.transactionIndex
        findAllTransIndex(all);
        //4.utxo
        findAllUtxo(all);
        //5.score
        findAllScore(all);
        //6.witness
//        findAllWitness(all);
        //7.witness block
        findAllWitnessBlock(all);


//        List<String> keys = scoreDao.allKeys();
//        if (CollectionUtils.isNotEmpty(keys)){
//            keys.forEach(key->LOGGER.info("score-key:"+ key));
//        }
//        scoreDao.deleteAll();
//        List<String> newKeys = scoreDao.allKeys();
//        if (CollectionUtils.isNotEmpty(newKeys)){
//            newKeys.forEach(key->LOGGER.info("new-score-key:"+ key));
//        }
//        fakeScore();
        LOGGER.info("ALL size() is {}", all.size());


        //clear all
//        if (!CollectionUtils.isEmpty(all)) {
//            all.forEach(base -> base.setValue(null));
//            blockDao.writeBatch(all);
//        }

    }


}
