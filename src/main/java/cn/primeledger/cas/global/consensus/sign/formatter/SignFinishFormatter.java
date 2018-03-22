package cn.primeledger.cas.global.consensus.sign.formatter;

import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import cn.primeledger.cas.global.consensus.sign.model.WitnessSign;
import cn.primeledger.cas.global.constants.EntityType;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

/**
 * @author zhao xiaogang
 * @deta 2018/3/6
 * @description
 */
@Component
public class SignFinishFormatter implements IEntityFormatter<WitnessSign> {

    @Override
    public EntityType getType() {
        return EntityType.BLOCK_CREATE_SIGN;
    }

    @Override
    public WitnessSign parse(String data, short version) {
        return JSON.parseObject(data, WitnessSign.class);
    }

    @Override
    public String format(WitnessSign data, short version) {
        return JSON.toJSONString(data);
    }
}
