package com.higgsblock.global.chain.app.keyvalue.db;

import org.iq80.leveldb.WriteBatch;

import java.io.Closeable;
import java.io.Serializable;
import java.util.Collection;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public interface ILevelDbWriteBatch extends Closeable {

    ILevelDbWriteBatch put(Serializable id, Object item, Serializable keyspace);

    ILevelDbWriteBatch put(Serializable indexName, Serializable index, Collection<Serializable> ids, Serializable keyspace);

    ILevelDbWriteBatch delete(Serializable id, Serializable keyspace);

    ILevelDbWriteBatch delete(Serializable indexName, Serializable index, Serializable keyspace);

    boolean isDeleted(Serializable id, Serializable keyspace);

    boolean isDeleted(Serializable indexName, Serializable index, Serializable keyspace);

    WriteBatch wrapper(WriteBatch writeBatch);
}
