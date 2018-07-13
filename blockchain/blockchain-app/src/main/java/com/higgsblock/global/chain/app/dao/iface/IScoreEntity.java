package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;

import java.util.List;

/**
 * @author yuanjiantao
 * @date 6/30/2018
 */
public interface IScoreEntity extends IDao<ScoreEntity> {
    int updateBatch(List<String> addressList, int score);

    int updateAll(int score);
}
