package cn.primeledger.cas.global.consensus.syncblock;

import cn.primeledger.cas.global.common.formatter.BaseEntityFormatter;
import cn.primeledger.cas.global.constants.EntityType;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Component
public class MaxHeightFormatter extends BaseEntityFormatter<MaxHeight> {

    @Override
    public EntityType getType() {
        return EntityType.MAXHEIGHT;
    }

    @Override
    public MaxHeight doParse(String data) {
        return JSON.parseObject(data, MaxHeight.class);
    }

    @Override
    public String doFormat(MaxHeight data) {
        return JSON.toJSONString(data);
    }
}
