package com.higgsblock.global.chain.app.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgsblock.global.chain.app.dao.entity.BaseDaoEntity;
import com.higgsblock.global.chain.app.utils.RocksDBWapper;
import org.rocksdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BaseDao including some basic database operation. All sub-dao classes have to extend
 * it and implement the 'getColumnFamilyName' method. The 'getColumnFamilyName' must be
 * contained by the ColumnFamilyDescriptor.
 *
 * @author zhao xiaogang
 * @create 2018-05-21
 */
public abstract class BaseDao<K, V> {

    @Autowired
    private RocksDBWapper rocksDBWapper;

    /**
     * the DB entity name like table name filled by
     * subclass implementing this function
     *
     * @return
     */
    protected abstract String getColumnFamilyName();

    public V get(K k) throws RocksDBException {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle(getColumnFamilyName());

        byte[] key = SerializationUtils.serialize(k);
        byte[] data = rocksDBWapper.getRocksDB().get(columnFamilyHandle, key);
        if (data != null) {
            String tempValue = (String) SerializationUtils.deserialize(data);
            return JSON.parseObject(tempValue, getValueClass());
        }

        return null;
    }

    public BaseDaoEntity getEntity(K k, V v) {
        return new BaseDaoEntity(k, v, getColumnFamilyName());
    }

    public RocksIterator iterator() {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle(getColumnFamilyName());
        return rocksDBWapper.getRocksDB().newIterator(columnFamilyHandle);
    }

    public List<byte[]> keys() {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle(getColumnFamilyName());

        final List<byte[]> keys = new ArrayList<>();
        final RocksIterator iterator = rocksDBWapper.getRocksDB().newIterator(columnFamilyHandle);
        for (iterator.seekToLast(); iterator.isValid(); iterator.prev()) {
            keys.add(iterator.key());
        }

        return keys;
    }

    public List<K> allKeys() {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle(getColumnFamilyName());

        final List<K> keys = new ArrayList<>();
        final RocksIterator iterator = rocksDBWapper.getRocksDB().newIterator(columnFamilyHandle);
        for (iterator.seekToLast(); iterator.isValid(); iterator.prev()) {
            keys.add((K) SerializationUtils.deserialize(iterator.key()));
        }

        return keys;
    }

    public List<V> allValues() {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle(getColumnFamilyName());

        final List<V> values = new ArrayList<>();
        final RocksIterator iterator = rocksDBWapper.getRocksDB().newIterator(columnFamilyHandle);
        for (iterator.seekToLast(); iterator.isValid(); iterator.prev()) {
            String tempValue = (String) SerializationUtils.deserialize(iterator.value());
            values.add(JSON.parseObject(tempValue, getValueClass()));
        }

        return values;
    }

    public void writeBatch(List<BaseDaoEntity> list) throws RocksDBException {
        try (WriteBatch batch = new WriteBatch()) {
            WriteOptions options = new WriteOptions();
            for (BaseDaoEntity item : list) {
                ColumnFamilyHandle handle = getColumnFamilyHandle(item.getColumnFamilyName());

                writeBatchByColumnFamily(item, batch, handle);
            }

            rocksDBWapper.getRocksDB().write(options, batch);
        }
    }

    public void writeBatch(BaseDaoEntity item) throws RocksDBException {
        try (WriteBatch batch = new WriteBatch()) {
            WriteOptions options = new WriteOptions();
            ColumnFamilyHandle handle = getColumnFamilyHandle(item.getColumnFamilyName());

            writeBatchByColumnFamily(item, batch, handle);

            rocksDBWapper.getRocksDB().write(options, batch);
        }
    }

    public void deleteAll() throws RocksDBException {
        ColumnFamilyHandle columnFamilyHandle = getColumnFamilyHandle(getColumnFamilyName());
        rocksDBWapper.getRocksDB().dropColumnFamily(columnFamilyHandle);
        try {
            columnFamilyHandle = rocksDBWapper.getRocksDB().createColumnFamily(new ColumnFamilyDescriptor(
                    getColumnFamilyName().getBytes("UTF-8"), new ColumnFamilyOptions()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        rocksDBWapper.getColumnFamilyHandleMap().put(getColumnFamilyName(), columnFamilyHandle);
    }

    public void delete(byte[] bytes) throws RocksDBException {
        rocksDBWapper.getRocksDB().delete(getColumnFamilyHandle(getColumnFamilyName()), bytes);
    }

    public void delete(K k) throws RocksDBException {
        rocksDBWapper.getRocksDB().delete(getColumnFamilyHandle(getColumnFamilyName()), SerializationUtils.serialize(k));
    }

    private ColumnFamilyHandle getColumnFamilyHandle(String columnFamilyName) {
        if (StringUtils.isEmpty(columnFamilyName)) {
            throw new IllegalStateException("Column family name can not be empty.");
        }

        Map<String, ColumnFamilyHandle> map = rocksDBWapper.getColumnFamilyHandleMap();
        ColumnFamilyHandle columnFamilyHandle = map.get(columnFamilyName);
        if (columnFamilyHandle == null) {
            throw new IllegalStateException("Invalid column family name: " + columnFamilyName);
        }

        return columnFamilyHandle;
    }

    private Class<V> getValueClass() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] entityClass = ((ParameterizedType) type).getActualTypeArguments();
            if (null != entityClass) {
                return (Class<V>) entityClass[1];
            }
        }
        return null;
    }

    private void writeBatchByColumnFamily(BaseDaoEntity item, WriteBatch batch, ColumnFamilyHandle handle) {
        if (item.getValue() == null) {
            byte[] key = SerializationUtils.serialize(item.getKey());
            batch.remove(handle, key);
        } else {
            byte[] key = SerializationUtils.serialize(item.getKey());
            String tempValue = JSON.toJSONString(item.getValue(),
                    SerializerFeature.DisableCircularReferenceDetect);
            byte[] value = SerializationUtils.serialize(tempValue);
            batch.put(handle, key, value);
        }
    }
}
