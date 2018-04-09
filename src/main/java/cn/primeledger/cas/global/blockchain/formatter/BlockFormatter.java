package cn.primeledger.cas.global.blockchain.formatter;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.common.formatter.BaseEntityFormatter;
import cn.primeledger.cas.global.constants.EntityType;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component
public class BlockFormatter extends BaseEntityFormatter<Block> {

    @Override
    public EntityType getType() {
        return EntityType.BLOCK_BROADCAST;
    }

    @Override
    public Block doParse(String data) {
        return JSON.parseObject(data, Block.class);
    }

    @Override
    public String doFormat(Block data) {
        return JSON.toJSONString(data);
    }
}
