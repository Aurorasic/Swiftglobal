package cn.primeledger.cas.global.consensus.syncblock;

import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import cn.primeledger.cas.global.constants.EntityType;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Component
public class MaxHeightFormatter implements IEntityFormatter<MaxHeight> {

    @Override
    public EntityType getType() {
        return EntityType.MAXHEIGHT;
    }

    @Override
    public MaxHeight parse(String data, short version) {
        return JSON.parseObject(data, MaxHeight.class);
    }

    @Override
    public String format(MaxHeight data, short version) {
        return JSON.toJSONString(data);
    }
}
