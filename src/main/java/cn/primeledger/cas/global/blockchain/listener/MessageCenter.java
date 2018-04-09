package cn.primeledger.cas.global.blockchain.listener;

import cn.primeledger.cas.global.common.SocketRequest;
import cn.primeledger.cas.global.common.entity.BroadcastMessageEntity;
import cn.primeledger.cas.global.common.entity.UnicastMessageEntity;
import cn.primeledger.cas.global.common.event.BroadcastEvent;
import cn.primeledger.cas.global.common.event.ReceivedDataEvent;
import cn.primeledger.cas.global.common.event.UnicastEvent;
import cn.primeledger.cas.global.common.formatter.BaseEntityFormatter;
import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import cn.primeledger.cas.global.common.handler.IEntityHandler;
import cn.primeledger.cas.global.common.listener.IEventBusListener;
import cn.primeledger.cas.global.constants.EntityType;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component
@Slf4j
public class MessageCenter implements IEventBusListener {

    @Resource(name = "asyncEventBus")
    private EventBus eventBus;

    @Autowired
    private Map<EntityType, IEntityFormatter> entityFormatterMap;

    @Autowired
    private Map<EntityType, IEntityHandler> entityHandlerMap;

    @Subscribe
    public void accept(ReceivedDataEvent event) {
        LOGGER.info("receive new ReceivedDataEvent {}", JSON.toJSONString(event));
        UnicastMessageEntity entity = event.getEntity();
        if (null == entity) {
            return;
        }
        try {
            String content = entity.getData();
            Preconditions.checkNotNull(content);

            EntityType entityType = BaseEntityFormatter.parseType(content);
            Preconditions.checkNotNull(entityType);

            IEntityFormatter formatter = getFormatter(entityType);
            Preconditions.checkNotNull(formatter);

            IEntityHandler handler = getEntityHandler(entityType);
            Preconditions.checkNotNull(handler);

            boolean result = handler.accept(new SocketRequest(entity.getSourceId(), formatter.parse(content)));
            if (!result) {
                LOGGER.info("queue is full, entityType={}", entityType);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public boolean accept(Object obj) {
        IEntityHandler handler = getHandler(obj.getClass());
        if (null != handler) {
            return handler.accept(new SocketRequest(null, obj));
        }
        return false;
    }

    public void send(Object event) {
        eventBus.post(event);
        LOGGER.info("send event: {}", event);
    }

    public void unicast(String sourceId, Object data) {
        UnicastMessageEntity entity = new UnicastMessageEntity();
        entity.setSourceId(sourceId);
        entity.setData(format(data));

        UnicastEvent event = new UnicastEvent(entity);
        eventBus.post(event);
        LOGGER.info("unicast message: {}", entity.getData());
    }

    public void broadcast(Object data) {
        broadcast(null, data);
    }

    public void broadcast(String[] excludeSourceIds, Object data) {
        BroadcastMessageEntity entity = new BroadcastMessageEntity();
        entity.setExcludeSourceIds(excludeSourceIds);
        entity.setData(format(data));

        BroadcastEvent event = new BroadcastEvent(entity);
        eventBus.post(event);
        LOGGER.info("broadcast message: {}", entity.getData());
    }

    private String format(Object data) {
        IEntityFormatter formatter = getFormatter(data.getClass());
        if (null != formatter) {
            return formatter.format(data);
        }
        LOGGER.warn("unsupported formatter");
        return null;
    }

    private IEntityFormatter getFormatter(Class<?> clazz) {
        return entityFormatterMap.values().stream().filter(formatter -> clazz == formatter.getEntityClass()).findFirst().orElse(null);
    }

    private IEntityHandler getHandler(Class<?> clazz) {
        return entityHandlerMap.values().stream().filter(handler -> clazz == handler.getEntityClass()).findFirst().orElse(null);
    }

    private IEntityFormatter getFormatter(EntityType type) {
        return entityFormatterMap.get(type);
    }

    private IEntityHandler getEntityHandler(EntityType type) {
        return entityHandlerMap.get(type);
    }

}
