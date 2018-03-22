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
public class InventoryFormatter implements IEntityFormatter<Inventory> {

    @Override
    public EntityType getType() {
        return EntityType.INVENTORY;
    }

    @Override
    public Inventory parse(String data, short version) {
        return JSON.parseObject(data, Inventory.class);
    }

    @Override
    public String format(Inventory data, short version) {
        return JSON.toJSONString(data);
    }
}
