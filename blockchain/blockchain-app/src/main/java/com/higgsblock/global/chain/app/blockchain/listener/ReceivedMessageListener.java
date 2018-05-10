package com.higgsblock.global.chain.app.blockchain.listener;

import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import com.higgsblock.global.chain.network.socket.event.ReceivedDataEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component
@Slf4j
public class ReceivedMessageListener implements IEventBusListener {

    @Autowired
    private MessageCenter messageCenter;

    @Subscribe
    public void process(ReceivedDataEvent event) {
        LOGGER.info("receive new ReceivedDataEvent {}", event);
        String content = event.getContent();
        String sourceId = event.getSourceId();
        boolean result = messageCenter.dispatch(sourceId, content);
        if (!result) {
            LOGGER.info("queue is full, content={}", content);
        }
    }

}
