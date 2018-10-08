package com.higgsblock.global.chain.app.blockchain.transaction.handler;

import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.ITransactionService;
import com.higgsblock.global.chain.network.socket.message.IMessage;
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
    protected boolean valid(IMessage<Transaction> message) {
        Transaction tx = message.getData();

        //step1 check transaction baseinfo
        if (!tx.valid()) {
            LOGGER.info("transaction is invalid:{}", tx.getHash());
            return false;
        }
        //step2 check transaction size
        if (!tx.sizeAllowed()) {
            LOGGER.info("Size of the transaction is illegal: {}", tx.getHash());
            return false;
        }

        if (!tx.validContractPart()) {
            LOGGER.info("Contract format is incorrect: {}", tx.getHash());
            return false;
        }

        return true;
    }

    @Override
    protected void process(IMessage<Transaction> message) {
        transactionService.receivedTransaction(message.getData());
    }
}