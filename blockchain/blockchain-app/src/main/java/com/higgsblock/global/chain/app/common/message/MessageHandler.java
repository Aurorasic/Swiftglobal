package com.higgsblock.global.chain.app.common.message;

import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.IEntityHandler;
import com.higgsblock.global.chain.app.constants.EntityType;
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
    private List<IEntityHandler<?>> handlerList;

    private Map<EntityType, IEntityHandler<?>> handlerMap = Maps.newHashMap();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isNotEmpty(handlerList)) {
            handlerList.forEach(this::register);
        }
    }

    public void register(IEntityHandler<?> handler) {
        EntityType type = formatter.getMessageType(handler.getEntityClass());
        handlerMap.put(type, handler);
        LOGGER.info("register IEntityFormatter, type={} ", type);
    }

    public void unregister(EntityType type) {
        handlerMap.remove(type);
        LOGGER.info("unregister IEntityFormatter, type={} ", type);
    }

    public IEntityHandler<?> getHandler(Class<?> clazz) {
        return getHandler(formatter.getMessageType(clazz));
    }

    public IEntityHandler<?> getHandler(EntityType type) {
        return handlerMap.get(type);
    }

    public boolean accept(SocketRequest request) {
        IEntityHandler handler = getHandler(request.getData().getClass());
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
