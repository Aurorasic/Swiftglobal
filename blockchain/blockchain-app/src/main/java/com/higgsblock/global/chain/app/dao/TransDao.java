package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.blockchain.transaction.TransactionIndex;
import org.springframework.stereotype.Repository;

/**
 * DAO for transaction index
 *
 * @author zhao xiaogang
 * @date 2018-05-22
 */

@Repository
public class TransDao extends BaseDao<String, TransactionIndex>{
    @Override
    protected String getColumnFamilyName() {
        return "transactionIndex";
    }
}
