package com.higgsblock.global.chain.network.socket.message;

import com.alibaba.fastjson.annotation.JSONType;

/**
 * @author baizhengwen
 * @date 2018-07-24
 */
@JSONType(includes = {"sourceId", "data"})
public class StringMessage extends BaseMessage<String> {

    public StringMessage() {
    }

    public StringMessage(String sourceId, String data) {
        super(sourceId, data);
    }
}
