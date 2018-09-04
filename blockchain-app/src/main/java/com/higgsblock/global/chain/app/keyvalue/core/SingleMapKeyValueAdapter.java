package com.higgsblock.global.chain.app.keyvalue.core;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.keyvalue.core.AbstractKeyValueAdapter;
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
public class SingleMapKeyValueAdapter extends AbstractKeyValueAdapter implements IndexedKeyValueAdapter {

    private Map<Serializable, Object> map = Maps.newConcurrentMap();

    @Override
    public Object put(Serializable id, Object item, Serializable keyspace) {
        return map.put(KeyValueAdapterUtils.getFullKey(keyspace, id), id);
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
    public void destroy() throws Exception {
        clear();
    }

    @Override
    public Collection<Serializable> saveIndex(String indexName, Serializable index, Serializable id, Serializable keyspace) {
        LOGGER.debug("saveIndex: keyspace={}, indexName={}, saveIndex={}", keyspace, indexName, index);
        Collection<Serializable> ids = findIdByIndex(indexName, index, keyspace);
        ids.add(id);
        map.put(KeyValueAdapterUtils.getFullKey(keyspace, indexName, index), ids);
        return ids;
    }

    @Override
    public Collection<Object> findByIndex(String indexName, Serializable index, Serializable keyspace) {
        Collection ids = findIdByIndex(indexName, index, keyspace);
        Map<Serializable, Object> map = Maps.newHashMap();
        Serializable key = null;
        for (Object id : ids) {
            key = (Serializable) id;
            map.putIfAbsent(key, get(key, keyspace));
        }
        return map.values();
    }

    @Override
    public Collection<Serializable> findIdByIndex(String indexName, Serializable index, Serializable keyspace) {
        LOGGER.debug("findIdByIndex: keyspace={}, indexName={}, saveIndex={}", keyspace, indexName, index);
        String key = KeyValueAdapterUtils.getFullKey(keyspace, indexName, index);
        Collection ids = (Collection<Serializable>) map.get(key);
        if (null == ids) {
            ids = Sets.newHashSet();
        }
        return ids;
    }
}
