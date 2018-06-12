package com.higgsblock.global.browser.service.iface;


import com.higgsblock.global.browser.dao.entity.TransactionPO;
import com.higgsblock.global.browser.service.bo.TransactionBO;
import com.higgsblock.global.browser.service.bo.TransactionItemsBO;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
public interface ITransactionService {

    /**
     * Get the transaction according to the hash.
     *
     * @param hash
     * @return
     */
    TransactionBO getTransactionByHash(String hash);

    /**
     * Gets the hash table of the transaction based on the block hash.
     *
     * @param blockHash
     * @return
     */
    List<String> getTxHashByBlockHash(String blockHash);

    /**
     * get TransactionItemsBO by pubKey
     *
     * @param pubKey
     * @return
     */
    TransactionItemsBO getTransactionByPk(String pubKey);

    /**
     * batch insert
     *
     * @param transactionPos
     */
    void batchInsert(List<TransactionPO> transactionPos);
}
