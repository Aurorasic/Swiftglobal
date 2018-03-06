package cn.primeledger.cas.global.common.formatter;


/**
 * @author baizhengwen
 * @date 2018/2/28
 */
public interface IEntityFormatter<T> {

    T parse(String data);

    String format(T data);
}
