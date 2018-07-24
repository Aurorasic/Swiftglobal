package com.higgsblock.global.chain.app.net;

import com.higgsblock.global.chain.app.common.message.MessageHandler;
import com.higgsblock.global.chain.network.socket.IMessageDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018-07-24
 */
@Component
public class MessageDispatcher implements IMessageDispatcher {

    @Autowired
    private MessageHandler handler;

    @Override
    public boolean dispatch(String channelId, String content) {
        return handler.accept(channelId, content);
    }
}
