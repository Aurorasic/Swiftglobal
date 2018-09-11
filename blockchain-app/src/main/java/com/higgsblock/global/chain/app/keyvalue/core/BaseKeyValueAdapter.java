package com.higgsblock.global.chain.app.keyvalue.core;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.keyvalue.core.AbstractKeyValueAdapter;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.keyvalue.core.QueryEngine;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-09-10
 */
@Slf4j
public abstract class BaseKeyValueAdapter extends AbstractKeyValueAdapter implements IndexedKeyValueAdapter {

    private Map<Serializable, Class> cache = Maps.newConcurrentMap();

    public BaseKeyValueAdapter() {
    }

    protected BaseKeyValueAdapter(QueryEngine<? extends KeyValueAdapter, ?, ?> engine) {
        super(engine);
    }

    @Override
    public Collection<Serializable> addIndex(String indexName, Serializable index, Serializable id, Serializable keyspace) {
        LOGGER.debug("addIndex: keyspace={}, indexName={}, index={}", keyspace, indexName, index);
        Collection<Serializable> ids = findIndex(indexName, index, keyspace);
        ids.add(id);
        saveIndex(indexName, index, ids, keyspace);
        return ids;
    }

    @Override
    public Collection<Serializable> deleteIndex(String indexName, Serializable index, Serializable id, Serializable keyspace) {
        LOGGER.debug("deleteIndex: keyspace={}, indexName={}, index={}", keyspace, indexName, index);
        Collection<Serializable> ids = findIndex(indexName, index, keyspace);
        ids.remove(id);
        saveIndex(indexName, index, ids, keyspace);
        return ids;
    }

    protected final void putEntityClass(Serializable keyspace, Class<?> clazz) {
        if (!cache.containsKey(keyspace)) {
            addEntityClass(keyspace, clazz);
            cache.putIfAbsent(keyspace, clazz);
        }
    }

    protected void addEntityClass(Serializable keyspace, Class<?> clazz) {

    }

    protected Class<?> getEntityClass(Serializable keyspace) {
        return cache.get(cache);
    }
}
