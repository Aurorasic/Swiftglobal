package com.higgsblock.global.chain.app.sync;

import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.common.constants.EntityType;
import com.higgsblock.global.chain.app.entity.BaseBizEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Message(EntityType.MAX_HEIGHT)
public class MaxHeight extends BaseBizEntity {
    private long maxHeight;

    @Override
    public boolean valid() {
        if (maxHeight < 0) {
            return false;
        }
        return true;
    }
}
