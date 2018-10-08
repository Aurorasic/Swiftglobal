package com.higgsblock.global.chain.app.keyvalue.core;

import org.springframework.data.keyvalue.core.KeyValueAdapter;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author baizhengwen
 * @date 2018-08-31
 */
public interface IndexedKeyValueAdapter extends KeyValueAdapter {

    Collection<Serializable> saveIndex(String indexName, Serializable index, Collection<Serializable> ids, Serializable keyspace);

    Collection<Serializable> addIndex(String indexName, Serializable index, Serializable id, Serializable keyspace);

    Collection<Serializable> deleteIndex(String indexName, Serializable index, Serializable id, Serializable keyspace);

    Collection<Serializable> findIndex(String indexName, Serializable index, Serializable keyspace);
}
