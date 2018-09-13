package com.higgsblock.global.chain.app.keyvalue.db;

import org.iq80.leveldb.WriteBatch;

import java.io.Closeable;
import java.io.Serializable;
import java.util.List;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public interface ILevelDbWriteBatch extends Closeable {

    Object get(Serializable key, Serializable keyspace);

    ILevelDbWriteBatch put(Serializable key, Object item, Serializable keyspace);

    ILevelDbWriteBatch delete(Serializable key, Serializable keyspace);

    boolean isDeleted(Serializable key, Serializable keyspace);

    WriteBatch wrapper(WriteBatch writeBatch);

    List<DataItem> copy();

    void clear();
}
