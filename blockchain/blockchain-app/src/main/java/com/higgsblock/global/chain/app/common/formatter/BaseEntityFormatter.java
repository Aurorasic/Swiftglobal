package com.higgsblock.global.chain.app.common.formatter;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author baizhengwen
 * @date 2018/3/27
 */
public abstract class BaseEntityFormatter<T> implements IEntityFormatter<T> {

    @Override
    public Class<T> getEntityClass() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] entityClass = ((ParameterizedType) type).getActualTypeArguments();
            if (null != entityClass) {
                return (Class<T>) entityClass[0];
            }
        }
        return null;
    }

    @Override
    public T parse(String data) {
        return JSON.parseObject(data, getEntityClass());
    }

    @Override
    public String format(T data) {
        return JSON.toJSONString(data);
    }
}
