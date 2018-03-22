package cn.primeledger.cas.global.consensus.sign.formatter;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import cn.primeledger.cas.global.constants.EntityType;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

/**
 * @author yangyi
 * @deta 2018/3/16
 * @description
 */
@Component
public class WitenessSignFormatter implements IEntityFormatter<Block> {

    @Override
    public EntityType getType() {
        return EntityType.BLOCK_COLLECT_SIGN;
    }

    @Override
    public Block parse(String data, short version) {
        return JSON.parseObject(data, Block.class);
    }

    @Override
    public String format(Block data, short version) {
        return JSON.toJSONString(data);
    }
}
