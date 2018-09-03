package com.higgsblock.global.chain.network.socket.event;

import com.alibaba.fastjson.annotation.JSONType;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.network.socket.message.StringMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JSONType(includes = {"message"})
public class ReceivedMessageEvent extends BaseSerializer {
    private StringMessage message;
}
