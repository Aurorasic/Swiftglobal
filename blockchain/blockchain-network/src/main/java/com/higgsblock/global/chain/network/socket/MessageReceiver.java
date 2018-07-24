package com.higgsblock.global.chain.network.socket;

import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import com.higgsblock.global.chain.network.socket.event.ReceivedMessageEvent;
import com.higgsblock.global.chain.network.socket.message.StringMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018-07-23
 */
@Slf4j
@Component
public class MessageReceiver implements IEventBusListener {

    @Autowired
    private IMessageDispatcher messageDispatcher;

    @Subscribe
    public boolean accept(ReceivedMessageEvent event) {
        StringMessage message = event.getMessage();
        String channelId = message.getSourceId();
        String content = message.getData();
        messageDispatcher.dispatch(channelId, content);
        LOGGER.info("received a new message, channelId={}, content={}", channelId, content);
        return true;
    }
}
