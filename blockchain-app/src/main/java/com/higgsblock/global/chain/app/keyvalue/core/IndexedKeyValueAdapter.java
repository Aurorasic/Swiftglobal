package com.higgsblock.global.chain.app.keyvalue.core;

import org.springframework.data.keyvalue.core.KeyValueAdapter;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author baizhengwen
 * @date 2018-08-31
 */
public interface IndexedKeyValueAdapter extends KeyValueAdapter {

    Collection<Serializable> saveIndex(String indexName, Serializable index, Serializable id, Serializable keyspace);

    Collection<Object> findByIndex(String indexName, Serializable index, Serializable keyspace);

    Collection<Serializable> findIdByIndex(String indexName, Serializable index, Serializable keyspace);
}
