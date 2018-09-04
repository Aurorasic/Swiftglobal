package com.higgsblock.global.chain.app.keyvalue.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.higgsblock.global.chain.app.keyvalue.db.ILevelDbWriteBatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.keyvalue.core.AbstractKeyValueAdapter;
import org.springframework.data.keyvalue.core.ForwardingCloseableIterator;
import org.springframework.data.util.CloseableIterator;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
@Slf4j
public class TransactionAwareLevelDbAdapter extends AbstractKeyValueAdapter implements ITransactionAwareKeyValueAdapter, IndexedKeyValueAdapter {

    private volatile boolean isAutoCommit = true;
    private volatile int transactionHolder;

    private ILevelDbWriteBatch writeBatch;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();

    private IndexedKeyValueAdapter mapAdapter;
    private LevelDbKeyValueAdapter levelDbAdapter;

    public TransactionAwareLevelDbAdapter(String dataPath) {
        super(new IndexedSpelQueryEngine());
        mapAdapter = new SingleMapKeyValueAdapter();
        levelDbAdapter = new LevelDbKeyValueAdapter(dataPath);
    }

    @Override
    public void beginTransaction() {
        LOGGER.debug("try to get a WriteLock");
        writeLock.lock();
        LOGGER.debug("get a WriteLock");
        try {
            if (transactionHolder == 0) {
                mapAdapter.clear();
                writeBatch = levelDbAdapter.createWriteBatch();
                transactionHolder = 1;
                isAutoCommit = false;
            } else {
                transactionHolder++;
            }

        } catch (Exception e) {
            rollbackTransaction();
        }
    }

    @Override
    public void rollbackTransaction() {
        try {
            if (transactionHolder > 1) {
                transactionHolder--;
            } else {
                mapAdapter.clear();
                writeBatch = null;
                transactionHolder = 0;
                isAutoCommit = true;
            }
        } finally {
            writeLock.unlock();
            LOGGER.debug("unlock a WriteLock");
        }
    }

    @Override
    public void commitTransaction() {
        try {
            if (transactionHolder > 1) {
                transactionHolder--;
            } else {
                mapAdapter.clear();
                levelDbAdapter.write(writeBatch);
                writeBatch = null;
                transactionHolder = 0;
                isAutoCommit = true;
            }
        } finally {
            writeLock.unlock();
            LOGGER.debug("unlock a WriteLock");
        }
    }


    protected <T> T doWithLock(Lock lock, Callable<T> callable) {
        LOGGER.debug("try to get a {}", lock.getClass().getSimpleName());
        lock.lock();
        LOGGER.debug("get a {}", lock.getClass().getSimpleName());
        try {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            lock.unlock();
            LOGGER.debug("unlock a {}", lock.getClass().getSimpleName());
        }
    }

    protected void doWithLock(Lock lock, Runnable runnable) {
        LOGGER.debug("try to get a {}", lock.getClass().getSimpleName());
        lock.lock();
        LOGGER.debug("get a {}", lock.getClass().getSimpleName());
        try {
            try {
                runnable.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } finally {
            lock.unlock();
            LOGGER.debug("unlock a {}", lock.getClass().getSimpleName());
        }
    }

    @Override
    public Object put(Serializable id, Object item, Serializable keyspace) {
        return doWithLock(writeLock, () -> {
            addEntityClass(keyspace, item.getClass());

            String key = KeyValueAdapterUtils.getFullKey(keyspace, id);
            String value = KeyValueAdapterUtils.toJsonString(item);
            if (isAutoCommit) {
                levelDbAdapter.put(id, value, keyspace);
            } else {
                mapAdapter.put(id, item, keyspace);
                writeBatch.put(key, value);
            }
            return item;
        });
    }

    @Override
    public boolean contains(Serializable id, Serializable keyspace) {
        return doWithLock(readLock, () -> null != get(id, keyspace));
    }

    @Override
    public Object get(Serializable id, Serializable keyspace) {
        return doWithLock(readLock, () -> {
            Object result = null;
            if (!isAutoCommit) {
                result = mapAdapter.get(id, keyspace);
            }
            if (null == result) {
                result = KeyValueAdapterUtils.parseJsonString((String) levelDbAdapter.get(id, keyspace), getEntityClass(keyspace));
            }
            return result;
        });
    }

    @Override
    public Object delete(Serializable id, Serializable keyspace) {
        return doWithLock(writeLock, () -> {
            Object result = null;
            if (!isAutoCommit) {
                result = mapAdapter.delete(id, keyspace);
                writeBatch.delete(KeyValueAdapterUtils.getFullKey(keyspace, id));
            } else {
                String value = (String) levelDbAdapter.delete(id, keyspace);
                result = KeyValueAdapterUtils.parseJsonString(value, getEntityClass(keyspace));
            }
            return result;
        });
    }

    @Override
    public Iterable<?> getAllOf(Serializable keyspace) {
        return doWithLock(readLock, (Callable<Iterable<?>>) () -> {
            List<Object> list = Lists.newLinkedList();
            entries(keyspace).forEachRemaining(entry -> list.add(entry.getValue()));
            return list;
        });
    }

    @Override
    public CloseableIterator<Map.Entry<Serializable, Object>> entries(Serializable keyspace) {
        return doWithLock(readLock, () -> {
            Map<Serializable, Object> map = Maps.newHashMap();
            Class<?> entityClass = getEntityClass(keyspace);
            String dataKeyPrefix = KeyValueAdapterUtils.getKeyPrefix(keyspace, true);

            levelDbAdapter.entries(keyspace)
                    .forEachRemaining(
                            entry -> {
                                Serializable key = entry.getKey();
                                if (key.toString().startsWith(dataKeyPrefix)) {
                                    map.put(key, KeyValueAdapterUtils.parseJsonString((String) entry.getValue(), entityClass));
                                }
                            }
                    );
            if (!isAutoCommit) {
                mapAdapter.entries(keyspace)
                        .forEachRemaining(
                                entry -> {
                                    Serializable key = entry.getKey();
                                    if (key.toString().startsWith(dataKeyPrefix)) {
                                        map.put(key, entry.getValue());
                                    }
                                }
                        );
            }

            return new ForwardingCloseableIterator<>(map.entrySet().iterator());
        });
    }

    @Override
    public void deleteAllOf(Serializable keyspace) {
        doWithLock(writeLock, () -> entries(keyspace).forEachRemaining(entry -> {
            // todo baizhengwen
            Serializable id = KeyValueAdapterUtils.getId(entry.getKey(), keyspace);
            delete(id, keyspace);
        }));
    }

    @Override
    public void clear() {
        doWithLock(writeLock, () -> {
            if (!isAutoCommit) {
                mapAdapter.clear();
            }
            levelDbAdapter.clear();
        });
    }

    @Override
    public long count(Serializable keyspace) {
        return doWithLock(readLock, () -> mapAdapter.count(keyspace) + levelDbAdapter.count(keyspace));
    }

    @Override
    public void destroy() throws Exception {
        doWithLock(writeLock, () -> {
            if (!isAutoCommit) {
                mapAdapter.destroy();
            }
            levelDbAdapter.destroy();
            return null;
        });
    }

    private void addEntityClass(Serializable keyspace, Class<?> clazz) {
        doWithLock(writeLock, () -> {
            String key = keyspace.toString();
            levelDbAdapter.put(key, clazz.getName(), KeyValueAdapterUtils.ENTITY_CLASS_KEY_SPACE);
        });
    }

    private Class<?> getEntityClass(Serializable keyspace) {
        return doWithLock(readLock, () -> {
            String key = keyspace.toString();
            String className = (String) levelDbAdapter.get(key, KeyValueAdapterUtils.ENTITY_CLASS_KEY_SPACE);
            return StringUtils.isEmpty(className) ? null : Class.forName(className);
        });
    }

    @Override
    public Collection<Serializable> saveIndex(String indexName, Serializable index, Serializable id, Serializable keyspace) {
        LOGGER.debug("saveIndex: keyspace={}, indexName={}, saveIndex={}", keyspace, indexName, index);
        return doWithLock(writeLock, () -> {
            Collection<Serializable> ids = findIdByIndex(indexName, index, keyspace);
            ids.add(id);
            String key = KeyValueAdapterUtils.getFullKey(keyspace, indexName, index);
            String value = KeyValueAdapterUtils.toJsonString(ids);

            if (isAutoCommit) {
                levelDbAdapter.saveIndex(indexName, index, id, keyspace);
            } else {
                mapAdapter.saveIndex(indexName, index, id, keyspace);

                writeBatch.put(key, value);
            }
            return ids;
        });
    }

    @Override
    public Collection<Object> findByIndex(String indexName, Serializable index, Serializable keyspace) {
        return doWithLock(readLock, () -> {
            Collection<Serializable> ids = findIdByIndex(indexName, index, keyspace);
            Map<Serializable, Object> map = Maps.newHashMap();
            for (Serializable id : ids) {
                map.putIfAbsent(id, get(id, keyspace));
            }
            return map.values();
        });
    }

    @Override
    public Collection<Serializable> findIdByIndex(String indexName, Serializable index, Serializable keyspace) {
        LOGGER.debug("findIdByIndex: keyspace={}, indexName={}, saveIndex={}", keyspace, indexName, index);
        return doWithLock(readLock, () -> {
            Set<Serializable> result = Sets.newHashSet();
            if (!isAutoCommit) {
                result.addAll(mapAdapter.findIdByIndex(indexName, index, keyspace));
            }
            result.addAll(levelDbAdapter.findIdByIndex(indexName, index, keyspace));
            return result;
        });
    }
}
