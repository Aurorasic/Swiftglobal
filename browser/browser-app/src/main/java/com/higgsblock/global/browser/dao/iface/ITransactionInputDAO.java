package com.higgsblock.global.browser.dao.iface;

import com.higgsblock.global.browser.dao.entity.TransactionInputPO;

import java.util.List;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
public interface ITransactionInputDAO extends IDAO<TransactionInputPO> {

    /**
     * batch insert
     *
     * @param transactionInputPos
     * @return
     */
    int[] batchInsert(List<TransactionInputPO> transactionInputPos);

    /**
     * get TransactionInputList by pubKey
     * @param pubKey
     * @return
     */
    List<String> getTxHashsByPubKey(String pubKey);

}
