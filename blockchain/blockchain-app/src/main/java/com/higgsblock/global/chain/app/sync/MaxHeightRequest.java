package com.higgsblock.global.chain.app.sync;

import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.app.entity.BaseBizEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@NoArgsConstructor
@Data
@Message(MessageType.MAX_HEIGHT_REQUEST)
public class MaxHeightRequest extends BaseBizEntity {
    @Override
    public boolean valid() {
        return true;
    }
}
