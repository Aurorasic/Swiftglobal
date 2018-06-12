package com.higgsblock.global.browser.dao.iface;

import com.higgsblock.global.browser.dao.entity.TransactionOutputPO;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
public interface ITransactionOutputDAO extends IDAO<TransactionOutputPO> {

    /**
     * batch insert
     *
     * @param transactionOutputPos
     * @return
     */
    int[] batchInsert(List<TransactionOutputPO> transactionOutputPos);

    /**
     * get TransactionOutputBoList by address
     *
     * @param address
     * @return
     */
    List<String> getTxHashsByAddress(String address);

    /**
     * get TransactionOutputList by txHash and index
     * @param transactionHash
     * @param index
     * @return
     */
    List<TransactionOutputPO> getTxOutput(String transactionHash, short index);
}
