package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import org.springframework.stereotype.Repository;

/**
 * @author Zhao xiaogang
 * @date 2018-05-22
 */
@Repository
public class BlockIndexBakDao extends BaseDao<Long, BlockIndex> {

    @Override
    protected String getColumnFamilyName() {
        return "blockIndexBak";
    }
}
