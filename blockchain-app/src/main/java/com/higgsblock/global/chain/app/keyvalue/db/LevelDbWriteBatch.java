package com.higgsblock.global.chain.app.keyvalue.db;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.higgsblock.global.chain.app.keyvalue.core.KeyValueAdapterUtils;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.WriteBatch;
import org.iq80.leveldb.impl.WriteBatchImpl;

import java.io.Serializable;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public class LevelDbWriteBatch implements ILevelDbWriteBatch {

    private String batchNo;
    private Table<Serializable, Serializable, Object> table = HashBasedTable.create();

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
    public boolean contains(Serializable id, Serializable keyspace) {
        return table.contains(keyspace, id);
    }

    @Override
    public Object get(Serializable id, Serializable keyspace) {
        return table.get(keyspace, id);
    }

    @Override
    public void put(Serializable id, Object item, Serializable keyspace) {
        table.put(keyspace, id, item);
    }

    @Override
    public void delete(Serializable id, Serializable keyspace) {
        table.put(keyspace, id, null);
    }

    @Override
    public Map<Serializable, Object> copy(Serializable keyspace) {
        return Maps.newHashMap(table.row(keyspace));
    }

    @Override
    public void clear() {
        table.clear();
    }

    @Override
    public WriteBatch wrapperAll() {
        WriteBatch batch = new WriteBatchImpl();
        byte[] key = null;
        byte[] value = null;
        for (Table.Cell<Serializable, Serializable, Object> cell : table.cellSet()) {
            key = SerializationUtils.serialize(KeyValueAdapterUtils.getBatchKey(cell.getRowKey(), cell.getColumnKey(), batchNo));
            value = SerializationUtils.serialize(KeyValueAdapterUtils.toJsonString(cell.getValue()));
            if (null == value) {
                batch.delete(key);
            } else {
                batch.put(key, value);
            }
        }
        return batch;
    }

    @Override
    public Map<Serializable, WriteBatch> wrapperByKeyspace() {
        Map<Serializable, WriteBatch> map = Maps.newHashMap();
        WriteBatch batch = null;
        byte[] key = null;
        byte[] value = null;
        for (Table.Cell<Serializable, Serializable, Object> cell : table.cellSet()) {
            batch = map.computeIfAbsent(cell.getRowKey(), serializable -> new WriteBatchImpl());
            key = SerializationUtils.serialize(cell.getColumnKey());
            value = SerializationUtils.serialize(KeyValueAdapterUtils.toJsonString(cell.getValue()));
            if (null == value) {
                batch.delete(key);
            } else {
                batch.put(key, value);
            }
        }
        return map;
    }
}
