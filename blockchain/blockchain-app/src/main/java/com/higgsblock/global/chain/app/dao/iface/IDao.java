package com.higgsblock.global.chain.app.dao.iface;

import java.util.List;

/**
 * generic interface to basic database operations.
 *
 * @author yangshenghong
 * @date 2018-05-08
 */
public interface IDao<T> {

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
    <E> T getByField(E e);

    /**
     * Query all data for the specified table.
     *
     * @return
     */
    List<T> findAll();
}
