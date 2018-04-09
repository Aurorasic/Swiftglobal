package cn.primeledger.cas.global.common.handler;

import cn.primeledger.cas.global.common.SocketRequest;
import cn.primeledger.cas.global.constants.EntityType;

import java.util.concurrent.ExecutorService;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
public interface IEntityHandler<T> {

    EntityType getType();

    Class<T> getEntityClass();

    void start();

    void start(ExecutorService executorService);

    void stop();

    boolean accept(SocketRequest<T> request);
}
