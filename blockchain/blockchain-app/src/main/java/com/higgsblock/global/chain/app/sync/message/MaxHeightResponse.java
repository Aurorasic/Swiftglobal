package com.higgsblock.global.chain.app.sync.message;

import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
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
@Message(MessageType.MAX_HEIGHT_RESPONSE)
public class MaxHeightResponse extends BaseSerializer {
    private long maxHeight;
}
