package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;
import com.higgsblock.global.chain.app.dao.iface.IScoreEntity;
import com.higgsblock.global.chain.app.service.IScoreService;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksDBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private IScoreEntity scoreDao;

    /**
     * get score by address
     *
     * @param address
     * @return
     */
    @Override
    public Integer get(String address) throws RocksDBException {
        ScoreEntity scoreEntity = scoreDao.getByField(address);
        return null == scoreEntity ? null : scoreEntity.getScore();
    }

    /**
     * set score
     *
     * @param address
     * @param score
     */
    @Override
    public void put(String address, Integer score) {
        scoreDao.add(new ScoreEntity(address, score));
    }

    /**
     * set score if not exist
     *
     * @param address
     * @param score
     * @return
     */
    @Override
    public void putIfAbsent(String address, Integer score) {

        ScoreEntity scoreEntity = scoreDao.getByField(address);
        if (scoreEntity == null) {
            scoreDao.add(new ScoreEntity(address, score));
        }
    }

    /**
     * remove score
     *
     * @param address
     */
    @Override
    public void remove(String address) {
        scoreDao.delete(address);
    }

    /**
     * query all score
     *
     * @return
     * @throws RocksDBException
     */
    @Override
    public Map<String, Integer> loadAll() {
        Map<String, Integer> map = new HashMap<>();
        scoreDao.findAll().forEach(e -> map.put(e.getAddress(), e.getScore()));
        return map;
    }
}
