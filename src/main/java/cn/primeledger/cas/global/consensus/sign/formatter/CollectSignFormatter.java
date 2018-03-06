package cn.primeledger.cas.global.consensus.sign.formatter;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

/**
 * @author zhao xiaogang
 * @deta 2018/3/6
 * @description
 */
@Component
public class CollectSignFormatter implements IEntityFormatter<Block> {
    @Override
    public Block parse(String data) {
        return JSON.parseObject(data, Block.class);
    }

    @Override
    public String format(Block data) {
        return JSON.toJSONString(data);
    }
}
