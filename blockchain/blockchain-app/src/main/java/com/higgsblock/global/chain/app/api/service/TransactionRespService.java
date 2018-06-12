package com.higgsblock.global.chain.app.api.service;

import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author kongyu
 * @date 2018-03-19
 */
@Slf4j
@Service
public class TransactionRespService {

    @Autowired
    private MessageCenter messageCenter;

    /**
     * Send transaction information
     *
     * @param tx
     * @return
     */
    public Boolean sendTransaction(Transaction tx) {
        LOGGER.info("sendTransaction start ...");
        if (null == tx) {
            return false;
        }
        LOGGER.info("transaction is = " + tx.toString());
        messageCenter.dispatch(tx);
        return true;
    }

}
