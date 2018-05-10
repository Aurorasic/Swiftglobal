package com.higgsblock.global.chain.app.blockchain.listener;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.message.MessageFormatter;
import com.higgsblock.global.chain.app.common.message.MessageHandler;
import com.higgsblock.global.chain.network.socket.event.BroadcastEvent;
import com.higgsblock.global.chain.network.socket.event.UnicastEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component
@Slf4j
public class MessageCenter {

    @Autowired
    private EventBus eventBus;
    @Autowired
    private MessageFormatter formatter;
    @Autowired
    private MessageHandler handler;

    public boolean dispatch(Object obj) {
        return handler.accept(new SocketRequest(null, obj));
    }

    public boolean dispatch(String sourceId, String obj) {
        return handler.accept(sourceId, obj);
    }

    public boolean unicast(String sourceId, Object data) {
        try {
            UnicastEvent event = new UnicastEvent();
            event.setSourceId(sourceId);
            event.setContent(formatter.format(data));

            eventBus.post(event);
            LOGGER.info("unicast message: {}", event.getContent());
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean broadcast(Object data) {
        return broadcast(null, data);
    }

    public boolean broadcast(String[] excludeSourceIds, Object data) {
        try {
            BroadcastEvent event = new BroadcastEvent();
            event.setExcludeSourceIds(excludeSourceIds);
            event.setContent(formatter.format(data));

            eventBus.post(event);
            LOGGER.info("broadcast message: {}", event.getContent());
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

}
