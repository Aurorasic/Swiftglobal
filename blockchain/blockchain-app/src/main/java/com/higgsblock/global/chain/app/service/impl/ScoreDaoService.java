package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.dao.ScoreDao;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import com.higgsblock.global.chain.app.service.IScoreService;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HuangShengli
 * @date 2018-05-23
 */
@Slf4j
@Service
public class ScoreDaoService implements IScoreService {

    @Autowired
    private ScoreDao scoreDao;

    /**
     * get score by address
     *
     * @param address
     * @return
     */
    @Override
    public Integer get(String address) throws RocksDBException {
        return scoreDao.get(address);
    }

    /**
     * set score
     *
     * @param address
     * @param score
     */
    @Override
    public BaseDaoEntity put(String address, Integer score) {
        return scoreDao.getEntity(address, score);
    }

    /**
     * set score if not exist
     *
     * @param address
     * @param score
     * @return
     */
    @Override
    public BaseDaoEntity putIfAbsent(String address, Integer score) throws RocksDBException {
        Integer loacalScore = scoreDao.get(address);
        if (loacalScore != null) {
            return scoreDao.getEntity(address, loacalScore);
        } else {
            return scoreDao.getEntity(address, score);
        }
    }

    /**
     * remove score
     *
     * @param address
     */
    @Override
    public BaseDaoEntity remove(String address) {
        return scoreDao.getEntity(address, null);
    }

    /**
     * query all score
     *
     * @return
     * @throws RocksDBException
     */
    @Override
    public Map<String, Integer> loadAll() throws RocksDBException {
        Map<String, Integer> scoreMap = new HashMap<>(1000);
        RocksIterator iterator = scoreDao.iterator();

        for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
            String key = (String) SerializationUtils.deserialize(iterator.key());
            String value = (String) SerializationUtils.deserialize(iterator.value());
            try {
                scoreMap.put(key, Integer.valueOf(value));
            } catch (NumberFormatException e) {
                LOGGER.error("score data format error from db,{}:{}", key, value);
                continue;
            }
        }
        return scoreMap;
    }
}
