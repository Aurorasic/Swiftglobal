package cn.primeledger.cas.global.common.handler;

/**
 * @author zhao xiaogang
 * @date 2018/3/8
 */
public abstract class UniqueEntityHandler<T> extends BaseEntityHandler<T> {

    @Override
    protected IEntityHandler getChildType() {
        return this;
    }
}
