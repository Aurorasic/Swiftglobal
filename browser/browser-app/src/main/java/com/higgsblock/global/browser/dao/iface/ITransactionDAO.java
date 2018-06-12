package com.higgsblock.global.browser.dao.iface;

import com.higgsblock.global.browser.dao.entity.TransactionPO;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
public interface ITransactionDAO extends IDAO<TransactionPO> {

    /**
     * batch insert
     *
     * @param transactionPos
     * @return
     */
    int[] batchInsert(List<TransactionPO> transactionPos);


    /**
     * Gets the hash table of the transaction based on the block hash.
     * @param blockHash
     * @return
     */
    List<String> getTxHashByBlockHash(String blockHash);
}
