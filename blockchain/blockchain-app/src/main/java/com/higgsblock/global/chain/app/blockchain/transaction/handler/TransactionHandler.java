package com.higgsblock.global.chain.app.blockchain.transaction.handler;

import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionService;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
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
public class TransactionHandler extends BaseEntityHandler<Transaction> {

    @Autowired
    private TransactionService transactionService;

    @Override
    protected void process(SocketRequest<Transaction> request) {
        transactionService.receivedTransaction(request.getData());
    }
}
