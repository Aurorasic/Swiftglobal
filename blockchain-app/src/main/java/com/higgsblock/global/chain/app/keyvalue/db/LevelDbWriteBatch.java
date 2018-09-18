package com.higgsblock.global.chain.app.keyvalue.db;

import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.keyvalue.core.KeyValueAdapterUtils;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.WriteBatch;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public class LevelDbWriteBatch implements ILevelDbWriteBatch {

    private String batchNo;
    private Map<Serializable, List<DataItem>> map = Maps.newConcurrentMap();

    public LevelDbWriteBatch() {
    }

    public LevelDbWriteBatch(String batchNo) {
        this.batchNo = batchNo;
    }

    @Override
    public String getBatchNo() {
        return batchNo;
    }

    @Override
    public Object get(Serializable key, Serializable keyspace) {
        List<DataItem> data = getData(keyspace);
        DataItem item = null;
        for (int i = data.size() - 1; i >= 0; i--) {
            item = data.get(i);
            if (Equivalence.equals().equivalent(key, item.getKey())) {
                return item.getValue();
            }
        }
        return null;
    }

    @Override
    public void put(Serializable key, Object item, Serializable keyspace) {
        getData(keyspace).add(new DataItem(keyspace, key, item));
    }

    @Override
    public void delete(Serializable key, Serializable keyspace) {
        getData(keyspace).add(new DataItem(keyspace, key, null));
    }

    @Override
    public WriteBatch wrapper(WriteBatch writeBatch) {
        copy().forEach(item -> {
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
    public List<DataItem> copy(Serializable keyspace) {
        return Lists.newArrayList(getData(keyspace));
    }

    @Override
    public List<DataItem> copy() {
        LinkedList<DataItem> list = Lists.newLinkedList();
        for (List<DataItem> items : map.values()) {
            list.addAll(items);
        }
        return list;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public List<ILevelDbWriteBatch> splitByKeyspace() {
        List<ILevelDbWriteBatch> list = Lists.newLinkedList();
        ILevelDbWriteBatch batch = null;
        for (Map.Entry<Serializable, List<DataItem>> entry : map.entrySet()) {
            batch = new LevelDbWriteBatch(String.valueOf(entry.getKey()));
            for (DataItem item : entry.getValue()) {
                batch.put(item.getKey(), item.getValue(), item.getKeyspace());
            }
            list.add(batch);
        }
        return list;
    }

    private List<DataItem> getData(Serializable keyspace) {
        return map.computeIfAbsent(keyspace, (Function<Serializable, List<DataItem>>) input -> Collections.synchronizedList(Lists.newLinkedList()));
    }
}
