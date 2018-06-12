package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.blockchain.LatestBestBlockIndex;
import org.springframework.stereotype.Repository;

/**
 * @author zhao xiaogang
 * @date 2018/5/31
 */
@Repository
public class LatestBlockIndexDao extends BaseDao<String, LatestBestBlockIndex> {
    @Override
    protected String getColumnFamilyName() {
        return "dict";
    }
}
