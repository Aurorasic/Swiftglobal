package cn.primeledger.cas.global.blockchain.transaction.formatter;

import cn.primeledger.cas.global.blockchain.transaction.Transaction;
import cn.primeledger.cas.global.common.formatter.BaseEntityFormatter;
import cn.primeledger.cas.global.constants.EntityType;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

/**
 * @author yangyi
 * @deta 2018/3/1
 * @description
 */
@Component
public class TransactionFormatter extends BaseEntityFormatter<Transaction> {

    @Override
    public EntityType getType() {
        return EntityType.TRANSACTION_TRANSFER_BROADCAST;
    }

    @Override
    public Transaction doParse(String data) {
        return JSON.parseObject(data, Transaction.class);
    }

    @Override
    public String doFormat(Transaction data) {
        return JSON.toJSONString(data);
    }
}
