package com.higgsblock.global.chain.app.keyvalue.core;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public class KeyValueAdapterUtils {

    public static final String DEFAULT_INDEX_NAME = "id";
    public static final String ENTITY_CLASS_KEY_SPACE = "_EntityClass";

    private KeyValueAdapterUtils() {
    }

    public static String getId(Serializable key, Serializable keyspace) {
        return getIndex(key, keyspace, DEFAULT_INDEX_NAME);
    }

    public static String getIndex(Serializable key, Serializable keyspace, String indexName) {
        return StringUtils.substringAfter(String.valueOf(key), String.format("%s_%s_", keyspace, indexName));
    }

    public static String getFullKey(Serializable keyspace, Serializable id) {
        return getFullKey(keyspace, DEFAULT_INDEX_NAME, id);
    }

    public static String getFullKey(Serializable keyspace, Serializable indexName, Serializable index) {
        return String.format("%s_%s_%s", keyspace, indexName, index);
    }

    public static String toJsonString(Object value) {
        return JSON.toJSONString(value);
    }

    public static <T> T parseJsonString(String value, Class<T> clazz) {
        return JSON.parseObject(value, clazz);
    }

    public static Collection parseJsonArrayString(String value, Class<? extends Serializable> clazz) {
        return JSON.parseArray(value, clazz);
    }

    public static String getKeyPrefix(Serializable keyspace) {
        return getKeyPrefix(keyspace, true);
    }

    public static String getKeyPrefix(Serializable keyspace, boolean isId) {
        if (isId) {
            return getKeyPrefix(keyspace, DEFAULT_INDEX_NAME);
        }
        return String.format("%s_", keyspace);
    }

    public static String getKeyPrefix(Serializable keyspace, Serializable indexName) {
        return String.format("%s_%s_", keyspace, indexName);
    }
}
