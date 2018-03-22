package cn.primeledger.cas.global.common.handler;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Data
@AllArgsConstructor
public abstract class BaseEntityHandler<T> implements IEntityHandler<T> {

    protected abstract IEntityHandler getChildType();

    @Override
    public void queueElementConsumeOver() {

    }
}
