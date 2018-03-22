package cn.primeledger.cas.global.blockchain.transaction.handler;

import cn.primeledger.cas.global.blockchain.transaction.Transaction;
import cn.primeledger.cas.global.blockchain.transaction.TransactionService;
import cn.primeledger.cas.global.common.handler.BroadcastEntityHandler;
import cn.primeledger.cas.global.constants.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yangyi
 * @deta 2018/3/1
 * @description
 */
@Slf4j
@Component("transactionHandler")
public class TransactionHandler extends BroadcastEntityHandler<Transaction> {

    @Autowired
    private TransactionService transactionService;

    @Override
    public EntityType getType() {
        return EntityType.TRANSACTION_TRANSFER_BROADCAST;
    }

    @Override
    public void process(Transaction data, short version, String sourceId) {
        transactionService.receivedTransaction(data);
    }
}
