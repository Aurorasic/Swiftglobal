package com.higgsblock.global.chain.app.keyvalue.db;

import org.iq80.leveldb.DBException;
import org.iq80.leveldb.ReadOptions;
import org.iq80.leveldb.Snapshot;
import org.iq80.leveldb.WriteOptions;

import java.io.Closeable;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public interface ILevelDb<T extends Serializable> extends Iterable<Map.Entry<String, T>>, Closeable {

    T get(String key) throws DBException;

    T get(String key, ReadOptions options) throws DBException;

    void put(String key, T value) throws DBException;

    Snapshot put(String key, T value, WriteOptions options) throws DBException;

    void delete(String key) throws DBException;

    Snapshot delete(String key, WriteOptions options) throws DBException;

    ILevelDbWriteBatch createWriteBatch();

    void write(ILevelDbWriteBatch updates) throws DBException;

    Snapshot write(ILevelDbWriteBatch updates, WriteOptions options) throws DBException;

    Iterator<Map.Entry<String, T>> iterator(ReadOptions options);

}
