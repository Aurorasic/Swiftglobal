package cn.primeledger.cas.global.blockchain.formatter;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import cn.primeledger.cas.global.constants.EntityType;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component
public class BlockFormatter implements IEntityFormatter<Block> {

    @Override
    public EntityType getType() {
        return EntityType.BLOCK_BROADCAST;
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
