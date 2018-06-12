package com.higgsblock.global.chain.app.dao;

import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import org.springframework.stereotype.Repository;

/**
 * @author Zhao xiaogang
 * @date 2018-05-22
 */
@Repository
public class UtxoDao extends BaseDao<String, UTXO> {
    @Override
    protected String getColumnFamilyName() {
        return "utxo";
    }
}
