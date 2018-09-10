package com.higgsblock.global.chain.app.keyvalue.core;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.keyvalue.core.ForwardingCloseableIterator;
import org.springframework.data.util.CloseableIterator;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
@Slf4j
public class SingleMapKeyValueAdapter extends BaseKeyValueAdapter implements IndexedKeyValueAdapter {

    private Map<Serializable, Object> map = Maps.newConcurrentMap();

    @Override
    public Object put(Serializable id, Object item, Serializable keyspace) {
        return map.put(KeyValueAdapterUtils.getFullKey(keyspace, id), item);
    }

    @Override
    public boolean contains(Serializable id, Serializable keyspace) {
        return map.containsKey(KeyValueAdapterUtils.getFullKey(keyspace, id));
    }

    @Override
    public Object get(Serializable id, Serializable keyspace) {
        return map.get(KeyValueAdapterUtils.getFullKey(keyspace, id));
    }

    @Override
    public Object delete(Serializable id, Serializable keyspace) {
        return map.remove(KeyValueAdapterUtils.getFullKey(keyspace, id));
    }

    @Override
    public Iterable<?> getAllOf(Serializable keyspace) {
        String prefix = KeyValueAdapterUtils.getKeyPrefix(keyspace);
        return map.entrySet().stream()
                .filter(entry -> entry.getKey().toString().startsWith(prefix))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public CloseableIterator<Map.Entry<Serializable, Object>> entries(Serializable keyspace) {
        String prefix = KeyValueAdapterUtils.getKeyPrefix(keyspace);
        Iterator<Map.Entry<Serializable, Object>> iterator = map.entrySet().stream()
                .filter(entry -> entry.getKey().toString().startsWith(prefix))
                .filter(entry -> null != entry.getValue())
                .iterator();

        return new ForwardingCloseableIterator<>(iterator);
    }

    @Override
    public void deleteAllOf(Serializable keyspace) {
        entries(keyspace).forEachRemaining(entry -> map.remove(entry.getKey()));
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public long count(Serializable keyspace) {
        String prefix = KeyValueAdapterUtils.getKeyPrefix(keyspace);
        return map.keySet().stream()
                .filter(key -> key.toString().startsWith(prefix))
                .count();
    }

    @Override
    public void destroy() {
        clear();
    }

    @Override
    public Collection<Serializable> saveIndex(String indexName, Serializable index, Collection<Serializable> ids, Serializable keyspace) {
        LOGGER.debug("saveIndex: keyspace={}, indexName={}, index={}", keyspace, indexName, index);
        String key = KeyValueAdapterUtils.getFullKey(keyspace, indexName, index);
        if (ids.isEmpty()) {
            map.remove(key);
        } else {
            map.put(key, ids);
        }
        return ids;
    }

    @Override
    public Collection<Serializable> findIndex(String indexName, Serializable index, Serializable keyspace) {
        LOGGER.debug("findIndex: keyspace={}, indexName={}, index={}", keyspace, indexName, index);
        String key = KeyValueAdapterUtils.getFullKey(keyspace, indexName, index);
        return (Collection<Serializable>) map.getOrDefault(key, Sets.newHashSet());
    }

    @Override
    protected void addEntityClass(Serializable keyspace, Class<?> clazz) {
        // ignore
    }

    @Override
    protected Class<?> getEntityClass(Serializable keyspace) {
        // ignore
        return null;
    }
}
