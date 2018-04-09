package cn.primeledger.cas.global.common.formatter;


import cn.primeledger.cas.global.constants.EntityType;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
public interface IEntityFormatter<T> {

    Class<T> getEntityClass();

    EntityType getType();

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
