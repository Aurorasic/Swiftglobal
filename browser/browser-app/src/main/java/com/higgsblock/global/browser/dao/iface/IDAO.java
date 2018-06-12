package com.higgsblock.global.browser.dao.iface;

import java.util.List;

/**
 * generic interface to basic database operations.
 *
 * @param <T>
 * @author yangshenghong
 * @date 2018-05-08
 */
public interface IDAO<T> {

    /**
     * Add data to the tables specified by the database.
     *
     * @param t entity
     * @return
     */
    int add(T t);

    /**
     * Update the data for the database specified tables.
     *
     * @param t entity
     * @return
     */
    int update(T t);

    /**
     * Deletes the contents of the database specified table.
     *
     * @param e entity
     * @return
     */
    <E> int delete(E e);

    /**
     * Specify the contents of the table according to the field query database.
     *
     * @param e entity
     * @return
     */
    <E> List<T> getByField(E e);

    /**
     * Paging query data for the specified table.
     *
     * @param start
     * @param limit
     * @return
     */
    List<T> findByPage(int start, int limit);
}
