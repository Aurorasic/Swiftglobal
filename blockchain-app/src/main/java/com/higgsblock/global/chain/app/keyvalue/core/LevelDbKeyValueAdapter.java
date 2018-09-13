package com.higgsblock.global.chain.app.keyvalue.core;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.higgsblock.global.chain.app.keyvalue.db.ILevelDb;
import com.higgsblock.global.chain.app.keyvalue.db.ILevelDbWriteBatch;
import com.higgsblock.global.chain.app.keyvalue.db.LevelDb;
import com.higgsblock.global.chain.app.keyvalue.db.LevelDbWriteBatch;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.WriteOptions;
import org.springframework.data.keyvalue.core.ForwardingCloseableIterator;
import org.springframework.data.util.CloseableIterator;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-08-24
 */
@Slf4j
public class LevelDbKeyValueAdapter extends BaseKeyValueAdapter implements IndexedKeyValueAdapter {

    private static final String KEYSPACE_ENTITY_CLASS = "$EC";

    @Setter
    protected String dataDir;
    @Setter
    protected Options options;
    @Setter
    protected ReadOptions readOptions;
    @Setter
    protected WriteOptions writeOptions;
    @Setter
    protected Map<Serializable, ILevelDb<String>> dbMap = Maps.newConcurrentMap();

    public LevelDbKeyValueAdapter(String dataDir) {
        super(new IndexedSpelQueryEngine());

        this.dataDir = dataDir;
        this.options = new Options();
        readOptions = new ReadOptions();
        writeOptions = new WriteOptions();
    }

    @Override
    public Object put(Serializable id, Object item, Serializable keyspace) {
        putEntityClass(keyspace, item.getClass());
        String key = String.valueOf(id);
        String value = JSON.toJSONString(item);
        getDb(keyspace).put(key, value, writeOptions);
        return item;
    }

    @Override
    public boolean contains(Serializable id, Serializable keyspace) {
        return null != get(id, keyspace);
    }

    @Override
    public Object get(Serializable id, Serializable keyspace) {
        String key = String.valueOf(id);
        String value = getDb(keyspace).get(key, readOptions);
        return KeyValueAdapterUtils.parseJsonString(value, getEntityClass(keyspace));
    }

    @Override
    public Object delete(Serializable id, Serializable keyspace) {
        Object value = get(id, keyspace);
        String key = String.valueOf(id);
        getDb(keyspace).delete(key, writeOptions);
        return value;
    }

    @Override
    public Iterable<?> getAllOf(Serializable keyspace) {
        List<Object> list = Lists.newLinkedList();
        entries(keyspace).forEachRemaining(entry -> list.add(entry.getValue()));
        return list;
    }

    @Override
    public CloseableIterator<Map.Entry<Serializable, Object>> entries(Serializable keyspace) {
        Class<?> entityClass = getEntityClass(keyspace);
        Map<Serializable, Object> map = Maps.newHashMap();

        getDb(keyspace).iterator(readOptions).forEachRemaining(entry -> {
            String value = entry.getValue();
            if (null != value) {
                map.put(entry.getKey(), KeyValueAdapterUtils.parseJsonString(value, entityClass));
            }
        });

        return new ForwardingCloseableIterator<>(map.entrySet().iterator());
    }

    @Override
    public void deleteAllOf(Serializable keyspace) {
        ILevelDb<String> db = removeDb(keyspace);
        if (null != db) {
            db.destroy();
        }
    }

    @Override
    public void clear() {
        try {
            destroy();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public long count(Serializable keyspace) {
        return Lists.newArrayList(entries(keyspace)).size();
    }

    @Override
    public void destroy() {
        dbMap.values().forEach(db -> {
            try {
                db.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }

    public ILevelDbWriteBatch createWriteBatch(Serializable keyspace) {
        return new LevelDbWriteBatch(String.valueOf(keyspace));
    }

    public void write(ILevelDbWriteBatch writeBatch) {
        getDb(writeBatch.getBatchNo()).write(writeBatch, writeOptions);
    }

    @Override
    public Collection<Serializable> saveIndex(String indexName, Serializable index, Collection<Serializable> ids, Serializable keyspace) {
        LOGGER.debug("saveIndex: keyspace={}, indexName={}, index={}", keyspace, indexName, index);
        String key = String.valueOf(index);
        String indexKeyspace = KeyValueAdapterUtils.getIndexKeyspace(keyspace, indexName);
        ILevelDb<String> db = getDb(indexKeyspace);
        if (ids.isEmpty()) {
            db.delete(key);
        } else {
            db.put(key, KeyValueAdapterUtils.toJsonString(ids));
        }
        return ids;
    }

    @Override
    public Collection<Serializable> findIndex(String indexName, Serializable index, Serializable keyspace) {
        LOGGER.debug("findIndex: keyspace={}, indexName={}, index={}", keyspace, indexName, index);
        String key = String.valueOf(index);
        String indexKeyspace = KeyValueAdapterUtils.getIndexKeyspace(keyspace, indexName);
        Collection<Serializable> ids = KeyValueAdapterUtils.parseJsonArrayString(getDb(indexKeyspace).get(key, readOptions), String.class);
        if (null == ids) {
            ids = Sets.newHashSet();
        }
        return ids;
    }

    @Override
    protected void addEntityClass(Serializable keyspace, Class<?> clazz) {
        String key = String.valueOf(keyspace);
        getEntityClassDb().put(key, clazz.getName(), writeOptions);
    }

    @Override
    protected Class<?> getEntityClass(Serializable keyspace) {
        Class<?> clazz = super.getEntityClass(keyspace);
        if (null != clazz) {
            return clazz;
        }
        String key = String.valueOf(keyspace);
        String className = getEntityClassDb().get(key, readOptions);
        try {
            clazz = StringUtils.isEmpty(className) ? null : Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return clazz;
    }

    public Map<String, String> stringEntries(Serializable keyspace) {
        Map<String, String> map = Maps.newHashMap();

        getDb(keyspace).iterator(readOptions).forEachRemaining(entry -> {
            map.put(entry.getKey(), entry.getValue());
        });
        return map;
    }

    protected String putString(Serializable id, String item, Serializable keyspace) {
        getDb(keyspace).put(String.valueOf(id), item, writeOptions);
        return item;
    }

    protected String getString(Serializable id, Serializable keyspace) {
        return getDb(keyspace).get(String.valueOf(id), readOptions);
    }

    protected ILevelDb<String> removeDb(Serializable keyspace) {
        return dbMap.remove(keyspace);
    }

    protected ILevelDb<String> getDb(Serializable keyspace) {
        return dbMap.computeIfAbsent(keyspace, db -> new LevelDb<>(dataDir + "/" + db, options));
    }

    protected ILevelDb<String> getEntityClassDb() {
        return getDb(KEYSPACE_ENTITY_CLASS);
    }
}
