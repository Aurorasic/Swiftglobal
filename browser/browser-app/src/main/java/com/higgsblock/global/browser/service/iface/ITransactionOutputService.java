package com.higgsblock.global.browser.service.iface;

import com.higgsblock.global.browser.dao.entity.TransactionOutputPO;
import com.higgsblock.global.browser.service.bo.TransactionItemsBO;

import java.util.List;


/**
 * @author Su Jiulong
 * @date 2018-05-24
 */
public interface ITransactionOutputService {
    /**
     * get transaction output by txHash and index
     *
     * @param transactionHash
     * @param index
     * @return
     */
    TransactionOutputPO getTxOutput(String transactionHash, short index);

    /**
     * batch insert
     *
     * @param transactionOutputPos
     * @return
     */
    void batchInsert(List<TransactionOutputPO> transactionOutputPos);

    /**
     * get transaction outputs by hash
     *
     * @param hash
     * @return
     */
    List<TransactionOutputPO> getTransactionOutPuts(String hash);

    /**
     * get TransactionOutputBoList by pubKey
     *
     * @param pubKey
     * @return
     */
    TransactionItemsBO getTxOutputBosByPubKey(String pubKey);

}
