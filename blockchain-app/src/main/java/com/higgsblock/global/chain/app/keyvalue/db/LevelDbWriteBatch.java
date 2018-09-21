package com.higgsblock.global.chain.app.keyvalue.db;

import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.keyvalue.core.KeyValueAdapterUtils;
import lombok.Data;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.impl.WriteBatchImpl;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Map;


/**
 * @author baizhengwen
 * @date 2018-08-29
 */
@Data
public class LevelDbWriteBatch implements ILevelDbWriteBatch {

    private String batchNo;
    private Map<Serializable, Map<Serializable, String>> map = Maps.newHashMap();

    public LevelDbWriteBatch(String batchNo) {
        this.batchNo = batchNo;
    }

    @Override
    public String getBatchNo() {
        return batchNo;
    }

    @Override
    public boolean contains(Serializable id, Serializable keyspace) {
        return getData(keyspace).containsKey(id);
    }

    @Override
    public Object get(Serializable id, Serializable keyspace, Type type) {
        return KeyValueAdapterUtils.parseJsonString(getData(keyspace).get(id), type);
    }

    @Override
    public void put(Serializable id, Object item, Serializable keyspace) {
        getData(keyspace).put(id, KeyValueAdapterUtils.toJsonString(item));
    }

    @Override
    public void delete(Serializable id, Serializable keyspace) {
        getData(keyspace).put(id, null);
    }

    @Override
    public Map<Serializable, Object> copy(Serializable keyspace, Type type) {
        Map<Serializable, Object> result = Maps.newHashMap();
        for (Map.Entry<Serializable, String> entry : getData(keyspace).entrySet()) {
            result.put(entry.getKey(), KeyValueAdapterUtils.parseJsonString(entry.getValue(), type));
        }
        return result;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public WriteBatch wrapperAll() {
        WriteBatch batch = new WriteBatchImpl();
        byte[] key = null;
        byte[] value = null;
        for (Map.Entry<Serializable, Map<Serializable, String>> row : map.entrySet()) {
            for (Map.Entry<Serializable, String> cell : row.getValue().entrySet()) {
                key = SerializationUtils.serialize(KeyValueAdapterUtils.getBatchKey(row.getKey(), cell.getKey(), batchNo));
                value = SerializationUtils.serialize(cell.getValue());
                if (null == value) {
                    batch.delete(key);
                } else {
                    batch.put(key, value);
                }
            }
        }
        return batch;
    }

    @Override
    public Map<Serializable, WriteBatch> wrapperByKeyspace() {
        Map<Serializable, WriteBatch> result = Maps.newHashMap();
        WriteBatch batch = null;
        byte[] key = null;
        byte[] value = null;
        for (Map.Entry<Serializable, Map<Serializable, String>> row : map.entrySet()) {
            batch = result.computeIfAbsent(row.getKey(), serializable -> new WriteBatchImpl());
            for (Map.Entry<Serializable, String> cell : row.getValue().entrySet()) {
                key = SerializationUtils.serialize(cell.getKey());
                value = SerializationUtils.serialize(cell.getValue());
                if (null == value) {
                    batch.delete(key);
                } else {
                    batch.put(key, value);
                }
            }
        }
        return result;
    }

    private Map<Serializable, String> getData(Serializable keyspace) {
        return map.computeIfAbsent(keyspace, serializable -> Maps.newHashMap());
    }
}
