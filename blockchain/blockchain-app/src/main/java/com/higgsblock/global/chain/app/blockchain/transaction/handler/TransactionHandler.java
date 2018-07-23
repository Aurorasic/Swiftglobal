package com.higgsblock.global.chain.app.blockchain.transaction.handler;

import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.ITransactionService;
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
    private ITransactionService transactionService;

    @Override
    protected boolean check(SocketRequest<Transaction> request) {
        Transaction tx = request.getData();
        String hash = tx.getHash();

        //step1 check transaction baseinfo
        if (!tx.valid()) {
            return false;
        }
        //step2 check transaction size
        if (!tx.sizeAllowed()) {
            LOGGER.info("Size of the transaction is illegal.");
            return false;
        }
        return true;
    }

    @Override
    protected void process(SocketRequest<Transaction> request) {
        transactionService.receivedTransaction(request.getData());
    }

}
