package com.higgsblock.global.chain.app.common.formatter;


/**
 * @author baizhengwen
 * @date 2018/2/28
 */
public interface IMessageFormatter<T> {

    /**
     * get class of T
     *
     * @return
     */
    Class<T> getEntityClass();

    /**
     * parse string to object
     *
     * @param data
     * @return exclude type by default
     */
    T parse(String data);

    /**
     * format object to string
     *
     * @param data
     * @return exclude type by default
     */
    String format(T data);
}
