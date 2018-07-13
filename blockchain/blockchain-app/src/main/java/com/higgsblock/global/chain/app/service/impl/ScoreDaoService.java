package com.higgsblock.global.chain.app.service.impl;

import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;
import com.higgsblock.global.chain.app.dao.iface.IScoreEntity;
import com.higgsblock.global.chain.app.dao.iface.IScoreRepository;
import com.higgsblock.global.chain.app.service.IScoreService;
import lombok.extern.slf4j.Slf4j;
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
    private IScoreRepository scoreRepository;

    /**
     * get score by address
     *
     * @param address
     * @return
     */
    @Override
    public Integer get(String address) {
        ScoreEntity scoreEntity = scoreRepository.findByAddress(address);
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
        ScoreEntity scoreEntity = scoreRepository.findByAddress(address);
        if (null != scoreEntity) {
            scoreEntity.setScore(score);
            scoreRepository.save(scoreEntity);
        } else {
            ScoreEntity saveEntity = new ScoreEntity(address, score);
            scoreRepository.save(saveEntity);
        }
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

        ScoreEntity scoreEntity = scoreRepository.findByAddress(address);
        if (scoreEntity == null) {
            scoreRepository.save(new ScoreEntity(address, score));
        }
    }

    /**
     * remove score
     *
     * @param address
     */
    @Override
    public void remove(String address) {
        scoreRepository.deleteByAddress(address);
    }

    /**
     * query all score
     *
     * @return
     */
    @Override
    public Map<String, Integer> loadAll() {
        Map<String, Integer> map = new HashMap<>();
        scoreRepository.findAll().forEach(e -> map.put(e.getAddress(), e.getScore()));
        return map;
    }
}
