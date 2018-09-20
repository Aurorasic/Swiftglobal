package com.higgsblock.global.chain.app.keyvalue.core;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.higgsblock.global.chain.app.keyvalue.db.ILevelDb;
import com.higgsblock.global.chain.app.keyvalue.db.ILevelDbWriteBatch;
import com.higgsblock.global.chain.app.keyvalue.db.LevelDbWriteBatch;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.WriteOptions;
import org.springframework.data.keyvalue.core.ForwardingCloseableIterator;
import org.springframework.data.util.CloseableIterator;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-08-24
 */
@Slf4j
public class SingleLevelDbKeyValueAdapter extends BaseKeyValueAdapter {

    private static final String KEYSPACE_ENTITY_CLASS = "_EC";

    @Setter
    protected ReadOptions readOptions;
    @Setter
    protected WriteOptions writeOptions;
    @Setter
    protected ILevelDb<String> db;

    public SingleLevelDbKeyValueAdapter(ILevelDb<String> db) {
        super(new IndexedSpelQueryEngine());

        this.db = db;
        readOptions = new ReadOptions();
        writeOptions = new WriteOptions();
    }


    @Override
    public Object put(Serializable id, Object item, Serializable keyspace) {
        if (null == item) {
            delete(id, keyspace);
        }
        putEntityClass(keyspace, item.getClass());
        String key = KeyValueAdapterUtils.getInternalKey(keyspace, id);
        String value = JSON.toJSONString(item);
        db.put(key, value, writeOptions);
        return item;
    }

    @Override
    public Object get(Serializable id, Serializable keyspace) {
        String key = KeyValueAdapterUtils.getInternalKey(keyspace, id);
        String value = db.get(key, readOptions);
        return KeyValueAdapterUtils.parseJsonString(value, getEntityClass(keyspace));
    }

    @Override
    public Object delete(Serializable id, Serializable keyspace) {
        Object value = get(id, keyspace);
        String key = KeyValueAdapterUtils.getInternalKey(keyspace, id);
        db.delete(key, writeOptions);
        return value;
    }

    public Map<String, String> stringEntries(Serializable keyspace) {
        Map<String, String> map = Maps.newHashMap();
        db.iterator(readOptions).forEachRemaining(entry -> {
            String key = entry.getKey();
            if (KeyValueAdapterUtils.isInKeyspace(key, keyspace)) {
                String value = entry.getValue();
                if (null != value) {
                    map.put(KeyValueAdapterUtils.parseId(key), value);
                }
            }
        });

        return map;
    }

    @Override
    public CloseableIterator<Map.Entry<Serializable, Object>> entries(Serializable keyspace) {
        Class<?> entityClass = getEntityClass(keyspace);
        Map<Serializable, Object> map = Maps.newHashMap();

        db.iterator(readOptions).forEachRemaining(entry -> {
            String key = entry.getKey();
            if (KeyValueAdapterUtils.isInKeyspace(key, keyspace)) {
                String value = entry.getValue();
                if (null != value) {
                    map.put(KeyValueAdapterUtils.parseId(key), KeyValueAdapterUtils.parseJsonString(value, entityClass));
                }
            }
        });

        return new ForwardingCloseableIterator<>(map.entrySet().iterator());
    }

    @Override
    public void deleteAllOf(Serializable keyspace) {
        db.iterator(readOptions).forEachRemaining(entry -> {
            String key = entry.getKey();
            if (KeyValueAdapterUtils.isInKeyspace(key, keyspace)) {
                db.delete(key, writeOptions);
            }
        });

    }

    @Override
    public void clear() {
        db.destroy();
    }

    @Override
    public void destroy() throws Exception {
        db.close();
    }

    public ILevelDbWriteBatch createWriteBatch(String batchNo) {
        return new LevelDbWriteBatch(batchNo);
    }

    @Override
    public void write(Serializable keyspace, WriteBatch writeBatch) {
        db.write(writeBatch, writeOptions);
    }

    @Override
    public Collection<Serializable> saveIndex(String indexName, Serializable index, Collection<Serializable> ids, Serializable keyspace) {
        LOGGER.debug("saveIndex: keyspace={}, indexName={}, index={}", keyspace, indexName, index);
        String key = KeyValueAdapterUtils.getIndexKey(keyspace, indexName, index);
        db.put(key, KeyValueAdapterUtils.toJsonString(ids));
        return ids;
    }

    @Override
    public Collection<Serializable> findIndex(String indexName, Serializable index, Serializable keyspace) {
        LOGGER.debug("findIndex: keyspace={}, indexName={}, index={}", keyspace, indexName, index);
        String key = KeyValueAdapterUtils.getIndexKey(keyspace, indexName, index);
        Collection<Serializable> ids = KeyValueAdapterUtils.parseJsonArrayString(db.get(key, readOptions), Serializable.class);
        if (null == ids) {
            ids = Sets.newHashSet();
        }
        return ids;
    }

    @Override
    protected void addEntityClass(Serializable keyspace, Class<?> clazz) {
        String key = KeyValueAdapterUtils.getInternalKey(KEYSPACE_ENTITY_CLASS, keyspace);
        db.put(key, clazz.getName(), writeOptions);
    }

    @Override
    protected Class<?> getEntityClass(Serializable keyspace) {
        Class<?> clazz = super.getEntityClass(keyspace);
        if (null != clazz) {
            return clazz;
        }
        String key = KeyValueAdapterUtils.getInternalKey(KEYSPACE_ENTITY_CLASS, keyspace);
        String className = db.get(key, readOptions);
        try {
            clazz = StringUtils.isEmpty(className) ? null : Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return clazz;
    }
}
