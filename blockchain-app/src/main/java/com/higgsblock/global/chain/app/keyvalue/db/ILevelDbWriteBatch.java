package com.higgsblock.global.chain.app.keyvalue.db;

import org.iq80.leveldb.WriteBatch;

import java.io.Serializable;
import java.util.List;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public interface ILevelDbWriteBatch {

    String getBatchNo();

    Object get(Serializable key, Serializable keyspace);

    void put(Serializable key, Object item, Serializable keyspace);

    void delete(Serializable key, Serializable keyspace);

    WriteBatch wrapper(WriteBatch writeBatch);

    List<DataItem> copy(Serializable keyspace);

    List<DataItem> copy();

    void clear();

    List<ILevelDbWriteBatch> splitByKeyspace();
}
