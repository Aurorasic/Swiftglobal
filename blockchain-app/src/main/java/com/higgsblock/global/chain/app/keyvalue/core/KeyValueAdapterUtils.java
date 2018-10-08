package com.higgsblock.global.chain.app.keyvalue.core;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * @author baizhengwen
 * @date 2018-08-29
 */
public class KeyValueAdapterUtils {

    private static final String SEPARATOR_KEYSPACE_ID = ".";

    private KeyValueAdapterUtils() {
    }

    public static String toJsonString(Object value) {
        return JSON.toJSONString(value);
    }

    public static <T> T parseJsonString(String value, Type type) {
        return JSON.parseObject(value, type);
    }

    public static Collection parseJsonArrayString(String value, Class<? extends Serializable> clazz) {
        return JSON.parseArray(value, clazz);
    }

    public static boolean isInKeyspace(String internalKey, Serializable keyspace) {
        return internalKey.startsWith(String.valueOf(keyspace) + SEPARATOR_KEYSPACE_ID);
    }

    public static boolean isIdStartWith(String internalKey, String idPrefix) {
        String id = StringUtils.substringAfter(internalKey, SEPARATOR_KEYSPACE_ID);
        return StringUtils.startsWith(id, idPrefix);
    }

    public static String parseId(String key) {
        return StringUtils.substringAfter(key, SEPARATOR_KEYSPACE_ID);
    }

    public static String parseKeyspace(String key) {
        return StringUtils.substringBefore(key, SEPARATOR_KEYSPACE_ID);
    }

    public static String getInternalKey(Serializable keyspace, Serializable id) {
        return String.format("%s.%s", keyspace, id);
    }

    public static String getIndexKeyspace(Serializable keyspace, Serializable indexName) {
        return String.format("_%s#%s", keyspace, indexName);
    }

    public static String getIndexKey(Serializable keyspace, Serializable indexName, Serializable index) {
        return getInternalKey(getIndexKeyspace(keyspace, indexName), index);
    }

    public static String getBatchKeyspace(Serializable keyspace, String batchNo) {
        return String.format("%s.%s", batchNo, keyspace);
    }

    public static String getBatchKey(Serializable keyspace, Serializable id, String batchNo) {
        return getInternalKey(getBatchKeyspace(keyspace, batchNo), id);
    }

}
