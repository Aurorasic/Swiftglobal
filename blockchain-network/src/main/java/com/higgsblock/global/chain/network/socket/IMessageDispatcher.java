package com.higgsblock.global.chain.network.socket;

/**
 * @author baizhengwen
 * @date 2018-07-24
 */
public interface IMessageDispatcher {

    boolean dispatch(String channelId, String content);
}
