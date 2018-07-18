package com.higgsblock.global.chain.app.common.message;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.common.formatter.IEntityFormatter;
import com.higgsblock.global.chain.app.common.constants.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
public class MessageFormatter implements InitializingBean {

    private static final String SEPARATOR = "|";

    @Autowired
    private List<IEntityFormatter<?>> formatterList;

    private Map<EntityType, IEntityFormatter<?>> formatterMap = Maps.newHashMap();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isNotEmpty(formatterList)) {
            formatterList.forEach(this::register);
        }
    }

    public void register(IEntityFormatter<?> formatter) {
        EntityType type = getMessageType(formatter.getEntityClass());
        formatterMap.put(type, formatter);
        LOGGER.info("register IEntityFormatter, type={} ", type);
    }

    public void unregister(EntityType type) {
        formatterMap.remove(type);
        LOGGER.info("unregister IEntityFormatter, type={} ", type);
    }

    public EntityType getMessageType(Class<?> clazz) {
        Message annotation = clazz.getAnnotation(Message.class);
        Preconditions.checkNotNull(annotation, "Missing annotation: Message");

        EntityType type = annotation.value();
        Preconditions.checkNotNull(type, "type invalid, class=", clazz);

        return type;
    }

    public IEntityFormatter<?> getFormatter(Class<?> clazz) {
        return getFormatter(getMessageType(clazz));
    }

    public IEntityFormatter<?> getFormatter(EntityType type) {
        return formatterMap.get(type);
    }

    public <T> T parse(String data) {
        String typeCode = StringUtils.substringBefore(data, SEPARATOR);

        EntityType type = EntityType.getByCode(typeCode);
        Preconditions.checkNotNull(type, "type invalid, typeCode={}", typeCode);

        IEntityFormatter<?> formatter = getFormatter(type);
        Preconditions.checkNotNull(formatter, "unsupported formatter");

        String content = StringUtils.substringAfter(data, SEPARATOR);
        return (T) formatter.parse(content);
    }

    /**
     * format object to string
     *
     * @param data
     * @return exclude type by default
     */
    public <T> String format(T data) {
        EntityType type = getMessageType(data.getClass());
        IEntityFormatter<T> formatter = (IEntityFormatter<T>) getFormatter(type);
        Preconditions.checkNotNull(formatter, "unsupported formatter");

        return String.format("%s|%s", type.getCode(), formatter.format(data));
    }
}
