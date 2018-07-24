package com.higgsblock.global.chain.network.socket.message;

/**
 * @author baizhengwen
 * @date 2018-07-24
 */
public class StringMessage extends BaseMessage<String> {

    public StringMessage() {
    }

    public StringMessage(String sourceId, String data) {
        super(sourceId, data);
    }
}
