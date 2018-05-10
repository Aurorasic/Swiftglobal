package com.higgsblock.global.chain.app.common.handler;

import com.higgsblock.global.chain.app.common.SocketRequest;

import java.util.concurrent.ExecutorService;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
public interface IEntityHandler<T> {

    Class<T> getEntityClass();

    void start();

    void start(ExecutorService executorService);

    void stop();

    boolean accept(SocketRequest<T> request);
}
