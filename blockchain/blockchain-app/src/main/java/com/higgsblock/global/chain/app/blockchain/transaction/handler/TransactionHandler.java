package com.higgsblock.global.chain.app.blockchain.transaction.handler;

import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionProcessor;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yangyi
 * @deta 2018/3/1
 * @description
 */
@Slf4j
@Component
public class TransactionHandler extends BaseMessageHandler<Transaction> {

    @Autowired
    private TransactionProcessor transactionProcessor;

    @Override
    protected boolean check(SocketRequest<Transaction> request) {
        Transaction tx = request.getData();
        String hash = tx.getHash();

        //step1 check transaction baseinfo
        if (!tx.valid()) {
            return false;
        }
        //step2 check transaction version
        int version = tx.getVersion();
        if (version < 0) {
            return false;
        }
        //step3 check transaction size
        if (!tx.sizeAllowed()) {
            LOGGER.info("Size of the transaction is illegal.");
            return false;
        }
        return false;
    }

    @Override
    protected void process(SocketRequest<Transaction> request) {
        transactionProcessor.receivedTransaction(request.getData());
    }

}
