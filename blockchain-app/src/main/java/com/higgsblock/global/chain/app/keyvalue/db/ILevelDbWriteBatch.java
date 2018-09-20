package com.higgsblock.global.chain.app.keyvalue.db;

import org.iq80.leveldb.WriteBatch;

import java.io.Serializable;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public interface ILevelDbWriteBatch {

    String getBatchNo();

    boolean contains(Serializable id, Serializable keyspace);

    Object get(Serializable key, Serializable keyspace);

    void put(Serializable key, Object item, Serializable keyspace);

    void delete(Serializable key, Serializable keyspace);

    Map<Serializable, Object> copy(Serializable keyspace);

    void clear();

    WriteBatch wrapperAll();

    Map<Serializable, WriteBatch> wrapperByKeyspace();
}
