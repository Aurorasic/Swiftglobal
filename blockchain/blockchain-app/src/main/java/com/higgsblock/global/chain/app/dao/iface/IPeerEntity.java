package com.higgsblock.global.chain.app.dao.iface;


import com.higgsblock.global.chain.app.dao.entity.PeerEntity;

/**
 * @author yangshenghong
 * @date 2018-05-08
 */
public interface IPeerEntity extends IDao<PeerEntity> {
    /**
     * Query all peer quantities.
     *
     * @return
     */
    Integer getCount();


    /**
     * According to the id update retries
     *
     * @param peerEntity
     * @return
     */
    int updateRetriesById(PeerEntity peerEntity);

    /**
     * delete all data
     *
     * @return
     */
    int deleteAll();
}
