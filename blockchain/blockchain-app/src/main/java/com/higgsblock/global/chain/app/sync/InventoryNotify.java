package com.higgsblock.global.chain.app.sync;

import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.entity.BaseBizEntity;
import lombok.Data;

import java.util.Set;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Data
@Message(MessageType.INVENTORY_NOTIFY)
public class InventoryNotify extends BaseBizEntity {

    private long height;

    private Set<String> hashs;

    @Override
    public boolean valid() {
        if (height < 0) {
            return false;
        }
        return true;
    }
}
