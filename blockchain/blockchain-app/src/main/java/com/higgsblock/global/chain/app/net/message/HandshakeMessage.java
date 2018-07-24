package com.higgsblock.global.chain.app.net.message;

import com.higgsblock.global.chain.network.socket.message.BaseMessage;
import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/3/27
 */
@Data
public class HandshakeMessage<T> extends BaseMessage<T> {

    public HandshakeMessage() {
    }

    public HandshakeMessage(String sourceId, T data) {
        super(sourceId, data);
    }

    public String getChannelId() {
        return getSourceId();
    }
}
