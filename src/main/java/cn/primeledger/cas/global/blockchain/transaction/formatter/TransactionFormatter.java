package cn.primeledger.cas.global.blockchain.transaction.formatter;

import cn.primeledger.cas.global.blockchain.transaction.Transaction;
import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import cn.primeledger.cas.global.constants.EntityType;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

/**
 * @author yangyi
 * @deta 2018/3/1
 * @description
 */
@Component
public class TransactionFormatter implements IEntityFormatter<Transaction> {

    @Override
    public EntityType getType() {
        return EntityType.TRANSACTION_TRANSFER_BROADCAST;
    }

    @Override
    public Transaction parse(String data, short version) {
        return JSON.parseObject(data, Transaction.class);
    }

    @Override
    public String format(Transaction data, short version) {
        return JSON.toJSONString(data);
    }
}
