package cn.primeledger.cas.global.common.handler;

import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Data
@AllArgsConstructor
public abstract class BaseEntityHandler<T> implements IEntityHandler<T> {

    @Override
    public final T parse(String data) {
        return getEntityFormatter().parse(data);
    }

    @Override
    public final String format(T data) {
        return getEntityFormatter().format(data);
    }

    @Override
    public final void process(String data) {
        T obj = parse(data);
        if (null != obj) {
            doProcess(obj);
        }
    }


    protected abstract IEntityFormatter<T> getEntityFormatter();

    protected abstract void doProcess(T data);
}
