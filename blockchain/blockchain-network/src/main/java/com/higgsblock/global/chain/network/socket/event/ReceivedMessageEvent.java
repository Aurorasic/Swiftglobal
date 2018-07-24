package com.higgsblock.global.chain.network.socket.event;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.network.socket.message.StringMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceivedMessageEvent extends BaseSerializer {
    private StringMessage message;
}
