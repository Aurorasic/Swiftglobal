package cn.primeledger.cas.global.blockchain.formatter;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import com.alibaba.fastjson.JSON;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */

public class BlockFormatter implements IEntityFormatter<Block> {

    @Override
    public Block parse(String data) {
        return JSON.parseObject(data, Block.class);
    }

    @Override
    public String format(Block data) {
        return JSON.toJSONString(data);
    }
}
