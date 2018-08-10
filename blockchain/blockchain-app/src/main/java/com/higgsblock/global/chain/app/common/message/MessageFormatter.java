package com.higgsblock.global.chain.app.common.message;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.common.formatter.IMessageFormatter;
import com.higgsblock.global.chain.app.common.constants.MessageType;
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
    private List<IMessageFormatter<?>> formatterList;

    private Map<MessageType, IMessageFormatter<?>> formatterMap = Maps.newHashMap();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isNotEmpty(formatterList)) {
            formatterList.forEach(this::register);
        }
    }

    public void register(IMessageFormatter<?> formatter) {
        MessageType type = getMessageType(formatter.getEntityClass());
        formatterMap.put(type, formatter);
        LOGGER.info("register IMessageFormatter, type={} ", type);
    }

    public void unregister(MessageType type) {
        formatterMap.remove(type);
        LOGGER.info("unregister IMessageFormatter, type={} ", type);
    }

    public MessageType getMessageType(Class<?> clazz) {
        Message annotation = clazz.getAnnotation(Message.class);
        Preconditions.checkNotNull(annotation, "Missing annotation: Message");

        MessageType type = annotation.value();
        Preconditions.checkNotNull(type, "type invalid, class=%s", clazz.getSimpleName());

        return type;
    }

    public IMessageFormatter<?> getFormatter(Class<?> clazz) {
        return getFormatter(getMessageType(clazz));
    }

    public IMessageFormatter<?> getFormatter(MessageType type) {
        return formatterMap.get(type);
    }

    public <T> T parse(String data) {
        String typeCode = StringUtils.substringBefore(data, SEPARATOR);

        MessageType type = MessageType.getByCode(typeCode);
        Preconditions.checkNotNull(type, "type invalid, typeCode=%s", typeCode);

        IMessageFormatter<?> formatter = getFormatter(type);
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
        MessageType type = getMessageType(data.getClass());
        IMessageFormatter<T> formatter = (IMessageFormatter<T>) getFormatter(type);
        Preconditions.checkNotNull(formatter, "unsupported formatter");

        return String.format("%s|%s", type.getCode(), formatter.format(data));
    }
}
