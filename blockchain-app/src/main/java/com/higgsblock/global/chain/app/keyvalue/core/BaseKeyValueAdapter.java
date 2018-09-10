package com.higgsblock.global.chain.app.keyvalue.core;

import com.google.common.collect.Maps;
import org.springframework.data.keyvalue.core.AbstractKeyValueAdapter;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.QueryEngine;

import java.io.Serializable;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-09-10
 */
public abstract class BaseKeyValueAdapter extends AbstractKeyValueAdapter {

    private Map<Serializable, Class> cache = Maps.newConcurrentMap();

    protected BaseKeyValueAdapter(QueryEngine<? extends KeyValueAdapter, ?, ?> engine) {
        super(engine);
    }

    protected final void putEntityClass(Serializable keyspace, Class<?> clazz) {
        if (!cache.containsKey(keyspace)) {
            addEntityClass(keyspace, clazz);
            cache.putIfAbsent(keyspace, clazz);
        }
    }

    protected abstract void addEntityClass(Serializable keyspace, Class<?> clazz);

    protected abstract Class<?> getEntityClass(Serializable keyspace);
}
