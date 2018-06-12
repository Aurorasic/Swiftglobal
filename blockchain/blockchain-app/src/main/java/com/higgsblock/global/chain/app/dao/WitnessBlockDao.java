package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.blockchain.Block;
import org.springframework.stereotype.Repository;

/**
 * @author HuangShengli
 * @date 2018-05-22
 */
@Repository
public class WitnessBlockDao extends BaseDao<Long, Block> {
    @Override
    protected String getColumnFamilyName() {
        return "witnessBlock";
    }


}
