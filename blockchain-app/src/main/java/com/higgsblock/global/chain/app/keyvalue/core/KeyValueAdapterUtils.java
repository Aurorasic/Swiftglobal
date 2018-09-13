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

    private KeyValueAdapterUtils() {
    }

    public static String getInternalKey(Serializable keyspace, Serializable key) {
        return String.format("%s_%s", keyspace, key);
    }

    public static String getIndexKey(Serializable keyspace, Serializable indexName, Serializable index) {
        return getInternalKey(getIndexKeyspace(keyspace, indexName), index);
    }

    public static String getIndexKeyspace(Serializable keyspace, Serializable indexName) {
        return String.format("_index.%s.%s", keyspace, indexName);
    }

    public static String getRealKeyspace(Serializable internalKey) {
        return StringUtils.substringBeforeLast(internalKey.toString(), "_");
    }

    public static String getRealKey(Serializable internalKey, Serializable keyspace) {
        return StringUtils.substringAfter(String.valueOf(internalKey), String.format("%s_", keyspace));
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
}
