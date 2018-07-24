package com.higgsblock.global.chain.network.socket;

/**
 * @author baizhengwen
 * @date 2018-07-24
 */
public interface IMessageSender<T> {

    boolean unicast(String channelId, T content);

    boolean broadcast(T content);

    boolean broadcast(String[] excludeChannelIds, T content);
}
