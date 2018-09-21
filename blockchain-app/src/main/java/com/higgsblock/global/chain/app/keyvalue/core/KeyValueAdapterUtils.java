package com.higgsblock.global.chain.app.keyvalue.core;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public class KeyValueAdapterUtils {

    private KeyValueAdapterUtils() {
    }

    public static String toJsonString(Object value) {
        return JSON.toJSONString(value);
    }

    public static <T> T parseJsonString(String value, Type type) {
        return JSON.parseObject(value, type);
    }

    protected static String getInternalKey(Serializable keyspace, Serializable id) {
        return String.format("%s.%s", keyspace, id);
    }

    protected static String getIndexKeyspace(Serializable keyspace, Serializable indexName) {
        return String.format("_%s#%s", keyspace, indexName);
    }

    protected static String getBatchKeyspace(Serializable keyspace, String batchNo) {
        return String.format("%s.%s", batchNo, keyspace);
    }

    public static String getBatchKey(Serializable keyspace, Serializable id, String batchNo) {
        return getInternalKey(getBatchKeyspace(keyspace, batchNo), id);
    }

}
