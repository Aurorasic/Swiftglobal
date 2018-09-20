package com.higgsblock.global.chain.app.keyvalue.db;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SerializationUtils;
import org.iq80.leveldb.*;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
@Slf4j
public class LevelDb<T extends Serializable> implements ILevelDb<T> {

    private DB db;
    private Options options;
    private String dataDir;

    public LevelDb(String dataDir, Options options) {
        this.dataDir = dataDir;
        this.options = options;

        File file = new File(dataDir);
        try {
            db = Iq80DBFactory.factory.open(file, options);
        } catch (Exception e) {
            LOGGER.error(String.format("%s, dataDir=%s", e.getMessage(), dataDir), e);
        }
    }

    @Override
    public void destroy() {
        try {
            close();
            Iq80DBFactory.factory.destroy(new File(dataDir), options);
        } catch (IOException e) {
            LOGGER.error(String.format("%s, dataDir=%s", e.getMessage(), dataDir), e);
        }
    }

    @Override
    public T get(String key) throws DBException {
        return deserialize(db.get(serialize(key)));
    }

    @Override
    public T get(String key, ReadOptions options) throws DBException {
        return deserialize(db.get(serialize(key), options));
    }

    @Override
    public void put(String key, T value) throws DBException {
        db.put(serialize(key), serialize(value));
    }

    @Override
    public Snapshot put(String key, T value, WriteOptions options) throws DBException {
        return db.put(serialize(key), serialize(value), options);
    }

    @Override
    public void delete(String key) throws DBException {
        db.delete(serialize(key));
    }

    @Override
    public Snapshot delete(String key, WriteOptions options) throws DBException {
        return db.delete(serialize(key), options);
    }

    @Override
    public void write(WriteBatch batch) throws DBException {
        db.write(batch);
    }

    @Override
    public Snapshot write(WriteBatch batch, WriteOptions options) throws DBException {
        return db.write(batch, options);
    }

    @Override
    public Iterator<Map.Entry<String, T>> iterator(ReadOptions options) {
        Map<String, T> map = Maps.newHashMap();
        db.iterator(options).forEachRemaining(
                entry -> map.put(String.valueOf(deserialize(entry.getKey())), deserialize(entry.getValue()))
        );
        return map.entrySet().iterator();
    }

    @Override
    public Iterator<Map.Entry<String, T>> iterator() {
        Map<String, T> map = Maps.newHashMap();
        db.iterator().forEachRemaining(
                entry -> map.put(String.valueOf(deserialize(entry.getKey())), deserialize(entry.getValue()))
        );
        return map.entrySet().iterator();
    }

    @Override
    public void close() throws IOException {
        db.close();
    }

    protected byte[] serialize(Serializable data) {
        return null == data ? new byte[0] : SerializationUtils.serialize(data);
    }

    protected T deserialize(byte[] bytes) {
        return ArrayUtils.isEmpty(bytes) ? null : (T) SerializationUtils.deserialize(bytes);
    }
}
