package com.higgsblock.global.chain.app.keyvalue.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.keyvalue.core.AbstractKeyValueAdapter;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.QueryEngine;

import java.io.Serializable;

/**
 * @author baizhengwen
 * @date 2018-09-10
 */
@Slf4j
public abstract class BaseKeyValueAdapter extends AbstractKeyValueAdapter {

    public BaseKeyValueAdapter(QueryEngine<? extends KeyValueAdapter, ?, ?> engine) {
        super(engine);
    }

    protected void addEntityClass(Serializable keyspace, Class<?> clazz) {
        String key = keyspace.toString();
        put(key, clazz.getName(), KeyValueAdapterUtils.ENTITY_CLASS_KEY_SPACE);
    }

    protected Class<?> getEntityClass(Serializable keyspace) {
        String key = keyspace.toString();
        String className = (String) get(key, KeyValueAdapterUtils.ENTITY_CLASS_KEY_SPACE);
        try {
            return StringUtils.isEmpty(className) ? null : Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

}
