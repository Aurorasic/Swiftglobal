package com.higgsblock.global.chain.app.sync.message;

import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@NoArgsConstructor
@Data
@Message(MessageType.MAX_HEIGHT_REQUEST)
public class MaxHeightRequest extends BaseSerializer {

    private int version = 0;

    public boolean valid() {
        return true;
    }
}
