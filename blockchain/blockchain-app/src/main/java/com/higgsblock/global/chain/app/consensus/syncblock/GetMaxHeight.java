package com.higgsblock.global.chain.app.consensus.syncblock;

import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.constants.EntityType;
import com.higgsblock.global.chain.app.entity.BaseBizEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@NoArgsConstructor
@Data
@Message(EntityType.GET_MAX_HEIGHT)
public class GetMaxHeight extends BaseBizEntity {
    @Override
    public boolean valid() {
        return true;
    }
}
