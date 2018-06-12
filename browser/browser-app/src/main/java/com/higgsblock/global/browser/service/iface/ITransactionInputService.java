package com.higgsblock.global.browser.service.iface;

import com.higgsblock.global.browser.dao.entity.TransactionInputPO;
import com.higgsblock.global.browser.service.bo.TransactionItemsBO;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-25
 */
public interface ITransactionInputService {
    /**
     * batch insert
     *
     * @param transactionInputPos
     * @return
     */
    void batchInsert(List<TransactionInputPO> transactionInputPos);

    /**
     * get TransactionInputBoList by pubKey
     *
     * @param pubKey
     * @return
     */
    TransactionItemsBO getTxInputBosByPubKey(String pubKey);

    /**
     * get TransactionInputList by pubKey
     *
     * @param hash
     * @return
     */
    List<TransactionInputPO> getByField(String hash);

}
