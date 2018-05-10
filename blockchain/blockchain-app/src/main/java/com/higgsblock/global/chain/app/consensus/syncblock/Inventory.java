package com.higgsblock.global.chain.app.consensus.syncblock;

import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.constants.EntityType;
import com.higgsblock.global.chain.app.entity.BaseBizEntity;
import lombok.Data;

import java.util.Set;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Data
@Message(EntityType.INVENTORY)
public class Inventory extends BaseBizEntity {

    private long height;

    private Set<String> hashs;

    @Override
    public boolean valid() {
        if (height < 0){
            return false;
        }
        return true;
    }
}
