package cn.primeledger.cas.global.common.handler;

import cn.primeledger.cas.global.constants.EntityType;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
public interface IEntityHandler<T> {

    EntityType getType();

    void process(T data, short version, String sourceId);

    /**
     * when the message queue has no data, this api should be called
     */
    void queueElementConsumeOver();
}
