package com.higgsblock.global.chain.app.sync.message;

import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Data;

import java.util.Set;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Data
@Message(MessageType.INVENTORY)
public class Inventory extends BaseSerializer {

    private long height;

    private Set<String> hashs;

    public boolean valid() {
        if (height < 0) {
            return false;
        }
        return true;
    }
}
