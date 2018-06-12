package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.network.Peer;
import org.springframework.stereotype.Repository;

/**
 * @author HuangShengli
 * @date 2018-05-22
 */
@Repository
public class PeerDao extends BaseDao<String, Peer> {

    @Override
    public String getColumnFamilyName() {
        return "peer";
    }
}
