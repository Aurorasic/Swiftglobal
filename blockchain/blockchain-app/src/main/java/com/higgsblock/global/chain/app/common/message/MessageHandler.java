package com.higgsblock.global.chain.app.common.message;

import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.IMessageHandler;
import com.higgsblock.global.chain.app.common.constants.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-05-04
 */
@Slf4j
@Component
public class MessageHandler implements InitializingBean {

    @Autowired
    private MessageFormatter formatter;

    @Autowired
    private List<IMessageHandler<?>> handlerList;

    private Map<MessageType, IMessageHandler<?>> handlerMap = Maps.newHashMap();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isNotEmpty(handlerList)) {
            handlerList.forEach(this::register);
        }
    }

    public void register(IMessageHandler<?> handler) {
        MessageType type = formatter.getMessageType(handler.getMessageClass());
        handlerMap.put(type, handler);
        LOGGER.info("register IMessageFormatter, type={} ", type);
    }

    public void unregister(MessageType type) {
        handlerMap.remove(type);
        LOGGER.info("unregister IMessageFormatter, type={} ", type);
    }

    public IMessageHandler<?> getHandler(Class<?> clazz) {
        return getHandler(formatter.getMessageType(clazz));
    }

    public IMessageHandler<?> getHandler(MessageType type) {
        return handlerMap.get(type);
    }

    public boolean accept(SocketRequest request) {
        IMessageHandler handler = getHandler(request.getData().getClass());
        if (null != handler) {
            return handler.accept(request);
        }
        return false;
    }

    public boolean accept(String sourceId, String message) {
        try {
            Object obj = formatter.parse(message);
            return accept(new SocketRequest(sourceId, obj));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

}
