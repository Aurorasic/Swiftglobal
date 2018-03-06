package cn.primeledger.cas.global.blockchain.transaction.handler;

import cn.primeledger.cas.global.blockchain.transaction.TransactionService;
import cn.primeledger.cas.global.blockchain.transaction.TransferTx;
import cn.primeledger.cas.global.blockchain.transaction.formatter.TransferTxFormatter;
import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import cn.primeledger.cas.global.common.handler.BaseEntityHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yangyi
 * @deta 2018/3/1
 * @description
 */
@Slf4j
@Component("transferTxHandler")
public class TransferTxHandler extends BaseEntityHandler<TransferTx> {

    @Autowired
    private TransferTxFormatter formatter;

    @Autowired
    private TransactionService transactionService;

    @Override
    protected IEntityFormatter<TransferTx> getEntityFormatter() {
        return formatter;
    }

    @Override
    protected void doProcess(TransferTx tx) {
        transactionService.receivedTransaction(tx);
    }
}
