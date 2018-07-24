package com.higgsblock.global.chain.app.net.message;

import com.higgsblock.global.chain.network.socket.message.BaseMessage;
import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/3/27
 */
@Data
public class BizMessage<T> extends BaseMessage<T> {

    public BizMessage() {
    }

    public BizMessage(String sourceId, T data) {
        super(sourceId, data);
    }
}
