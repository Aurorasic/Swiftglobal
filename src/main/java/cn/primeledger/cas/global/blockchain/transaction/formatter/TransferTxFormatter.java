package cn.primeledger.cas.global.blockchain.transaction.formatter;

import cn.primeledger.cas.global.blockchain.transaction.TransferTx;
import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

/**
 * @author yangyi
 * @deta 2018/3/1
 * @description
 */
@Component
public class TransferTxFormatter implements IEntityFormatter<TransferTx> {
    @Override
    public TransferTx parse(String data) {
        return JSON.parseObject(data, TransferTx.class);
    }

    @Override
    public String format(TransferTx data) {
        return JSON.toJSONString(data);
    }
}
