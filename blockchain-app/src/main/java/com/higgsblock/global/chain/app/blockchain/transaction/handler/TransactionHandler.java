package com.higgsblock.global.chain.app.blockchain.transaction.handler;

import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.service.ITransactionService;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import com.higgsblock.global.chain.network.socket.message.IMessage;
import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.GasCost;
import com.higgsblock.global.chain.vm.OpCode;
import com.higgsblock.global.chain.vm.api.ExecutionEnvironment;
import com.higgsblock.global.chain.vm.api.ExecutionResult;
import com.higgsblock.global.chain.vm.api.Executor;
import com.higgsblock.global.chain.vm.config.BlockchainConfig;
import com.higgsblock.global.chain.vm.config.Constants;
import com.higgsblock.global.chain.vm.core.*;
import com.higgsblock.global.chain.vm.program.Program;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        return true;
    }

    @Override
    protected void process(IMessage<Transaction> message) {
        transactionService.receivedTransaction(message.getData());

    }
}
