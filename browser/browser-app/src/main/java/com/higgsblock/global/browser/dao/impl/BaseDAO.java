package com.higgsblock.global.browser.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Common basic approach.
 *
 * @author yangshenghong
 * @date 2018-05-08
 */
public abstract class BaseDAO<T> {

    @Autowired
    protected NamedParameterJdbcTemplate template;

    /**
     * A common way to add data.
     *
     * @param t   entity
     * @param sql The SQL statement
     * @return
     */
    public int add(T t, String sql) {
        return template.update(sql, new BeanPropertySqlParameterSource(t));
    }

    /**
     * A common way to update data.
     *
     * @param t   entity
     * @param sql The SQL statement
     * @return
     */
    public int update(T t, String sql) {
        return template.update(sql, new BeanPropertySqlParameterSource(t));
    }

    /**
     * Delete according to the specified field.
     *
     * @param sql      The SQL statement
     * @param paramMap The data Map
     * @return
     */
    public int delete(String sql, Map<String, ?> paramMap) {
        return template.update(sql, paramMap);
    }

    /**
     * Generic query data based on fields.
     *
     * @param sql      The SQL statement
     * @param paramMap Field data
     * @return
     */
    public List<T> getByField(String sql, Map<String, ?> paramMap) {
        try {
            return template.query(sql, paramMap, new BeanPropertyRowMapper<>(getT()));
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * get all data
     *
     * @param sql The SQL statement
     * @return
     */
    public List<T> findAll(String sql) {
        return template.query(sql, new BeanPropertyRowMapper<>(getT()));
    }

    /**
     * Paging queries all data.
     *
     * @param paramMap The data Map
     * @param sql      The SQL statement
     * @return
     */
    public List<T> findByPage(Map<String, ?> paramMap, String sql) {
        return template.query(sql, paramMap, new BeanPropertyRowMapper<>(getT()));
    }

    private Class getT() {
        Class<? extends BaseDAO> clazz = this.getClass();
        ParameterizedType type = (ParameterizedType) clazz.getGenericSuperclass();
        Type[] types = type.getActualTypeArguments();
        if (null != types) {
            return (Class) types[0];
        }
        return null;
    }
}
