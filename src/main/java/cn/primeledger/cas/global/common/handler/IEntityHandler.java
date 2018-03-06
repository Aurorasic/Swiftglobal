package cn.primeledger.cas.global.common.handler;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
public interface IEntityHandler<T> {

    T parse(String data);

    String format(T data);

    void process(String data);

}
