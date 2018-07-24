package com.higgsblock.global.chain.network.socket.message;

/**
 * @author baizhengwen
 * @date 2018/3/27
 */

public interface IMessage<T> {

    String getSourceId();

    T getData();
}
