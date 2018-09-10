package com.higgsblock.global.chain.app.keyvalue.db;

import com.google.common.base.Equivalence;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.keyvalue.core.KeyValueAdapterUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.WriteBatch;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public class LevelDbWriteBatch implements ILevelDbWriteBatch {

    private final List<Map.Entry<Serializable, Serializable>> batch = Collections.synchronizedList(Lists.newLinkedList());

    @Override
    public ILevelDbWriteBatch put(Serializable id, Object item, Serializable keyspace) {
        String key = KeyValueAdapterUtils.getFullKey(keyspace, id);
        String value = KeyValueAdapterUtils.toJsonString(item);
        return put(key, value);
    }

    @Override
    public ILevelDbWriteBatch put(Serializable indexName, Serializable index, Collection<Serializable> ids, Serializable keyspace) {
        String key = KeyValueAdapterUtils.getFullKey(keyspace, indexName, index);
        if (CollectionUtils.isEmpty(ids)) {
            delete(key);
        } else {
            put(key, KeyValueAdapterUtils.toJsonString(ids));
        }
        return this;
    }

    @Override
    public ILevelDbWriteBatch delete(Serializable id, Serializable keyspace) {
        String key = KeyValueAdapterUtils.getFullKey(keyspace, id);
        return delete(key);
    }

    @Override
    public ILevelDbWriteBatch delete(Serializable indexName, Serializable index, Serializable keyspace) {
        String key = KeyValueAdapterUtils.getFullKey(keyspace, indexName, index);
        return delete(key);
    }

    @Override
    public boolean isDeleted(Serializable id, Serializable keyspace) {
        String key = KeyValueAdapterUtils.getFullKey(keyspace, id);
        return isDeleted(key);
    }

    @Override
    public boolean isDeleted(Serializable indexName, Serializable index, Serializable keyspace) {
        String key = KeyValueAdapterUtils.getFullKey(keyspace, indexName, index);
        return isDeleted(key);
    }

    @Override
    public WriteBatch wrapper(WriteBatch writeBatch) {
        batch.forEach(entry -> {
            byte[] key = SerializationUtils.serialize(entry.getKey());
            byte[] value = SerializationUtils.serialize(entry.getValue());
            if (null == value) {
                writeBatch.delete(key);
            } else {
                writeBatch.put(key, value);
            }
        });
        return writeBatch;
    }

    @Override
    public void close() {

    }

    private ILevelDbWriteBatch put(Serializable key, Serializable value) {
        batch.add(Maps.immutableEntry(key, value));
        return this;
    }

    private ILevelDbWriteBatch delete(Serializable key) {
        batch.add(Maps.immutableEntry(key, null));
        return this;
    }

    private boolean isDeleted(Serializable key) {
        Map.Entry<Serializable, Serializable> entry = null;
        for (int i = batch.size() - 1; i >= 0; i--) {
            entry = batch.get(i);
            if (Equivalence.equals().equivalent(key, entry.getKey())) {
                return null == entry.getValue();
            }
        }
        return false;
    }
}
