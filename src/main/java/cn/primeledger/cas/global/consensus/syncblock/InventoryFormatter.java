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
public class InventoryFormatter extends BaseEntityFormatter<Inventory> {

    @Override
    public EntityType getType() {
        return EntityType.INVENTORY;
    }

    @Override
    public Inventory doParse(String data) {
        return JSON.parseObject(data, Inventory.class);
    }

    @Override
    public String doFormat(Inventory data) {
        return JSON.toJSONString(data);
    }
}
