package com.higgsblock.global.chain.app.common.handler;

import com.higgsblock.global.chain.network.socket.message.IMessage;

import java.util.concurrent.ExecutorService;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
public interface IMessageHandler<T> {

    Class<T> getMessageClass();

    void start();

    void start(ExecutorService executorService);

    void stop();

    boolean accept(IMessage<T> message);
}
