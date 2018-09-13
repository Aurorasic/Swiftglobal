package com.higgsblock.global.chain.app.keyvalue.db;

import com.google.common.base.Equivalence;
import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.keyvalue.core.KeyValueAdapterUtils;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.WriteBatch;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public class LevelDbWriteBatch implements ILevelDbWriteBatch {

    private String batchNo;
    private List<DataItem> data = Collections.synchronizedList(Lists.newLinkedList());

    public LevelDbWriteBatch(String batchNo) {
        this.batchNo = batchNo;
    }

    @Override
    public String getBatchNo() {
        return batchNo;
    }

    @Override
    public Object get(Serializable key, Serializable keyspace) {
        DataItem item = null;
        for (int i = data.size() - 1; i >= 0; i--) {
            item = data.get(i);
            if (Equivalence.equals().equivalent(key, item.getKey())
                    && Equivalence.equals().equivalent(keyspace, item.getKeyspace())) {
                return item.getValue();
            }
        }
        return null;
    }

    @Override
    public ILevelDbWriteBatch put(Serializable key, Object item, Serializable keyspace) {
        data.add(new DataItem(keyspace, key, item));
        return this;
    }

    @Override
    public ILevelDbWriteBatch delete(Serializable key, Serializable keyspace) {
        data.add(new DataItem(keyspace, key, null));
        return this;
    }

    @Override
    public boolean isDeleted(Serializable key, Serializable keyspace) {
        return null == get(key, keyspace);
    }

    @Override
    public WriteBatch wrapper(WriteBatch writeBatch) {
        data.forEach(item -> {
            byte[] key = SerializationUtils.serialize(KeyValueAdapterUtils.getInternalKey(item.getKeyspace(), item.getKey()));
            byte[] value = SerializationUtils.serialize(KeyValueAdapterUtils.toJsonString(item.getValue()));
            if (null == value) {
                writeBatch.delete(key);
            } else {
                writeBatch.put(key, value);
            }
        });
        return writeBatch;
    }

    @Override
    public List<DataItem> copy() {
        return Lists.newArrayList(data);
    }

    @Override
    public void clear() {
        data.clear();
    }
}
