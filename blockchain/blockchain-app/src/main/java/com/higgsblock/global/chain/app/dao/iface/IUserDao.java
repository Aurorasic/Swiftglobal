package com.higgsblock.global.chain.app.dao.iface;

import com.higgsblock.global.chain.dao.entity.MinerScoreEntity;
import com.higgsblock.global.chain.dao.entity.PeerEntity;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018/4/20
 */
@Deprecated
public interface IUserDao {

    boolean add(MinerScoreEntity user);

    boolean update(MinerScoreEntity user);

    MinerScoreEntity getById(int id);

    List<PeerEntity> list(int start, int limit);
}
