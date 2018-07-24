package com.higgsblock.global.chain.app.net.message;

import com.higgsblock.global.chain.app.common.constants.MessageType;
import com.higgsblock.global.chain.app.common.message.Message;
import com.higgsblock.global.chain.network.Peer;
import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018-07-24
 */
@Message(MessageType.HELLO)
@Data
public class Hello {

    private Peer peer;

}
