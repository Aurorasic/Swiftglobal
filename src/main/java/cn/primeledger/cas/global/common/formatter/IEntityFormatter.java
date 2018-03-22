package cn.primeledger.cas.global.common.formatter;


import cn.primeledger.cas.global.constants.EntityType;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
public interface IEntityFormatter<T> {

    EntityType getType();

    T parse(String data, short version);

    String format(T data, short version);
}
