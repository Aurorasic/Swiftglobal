package com.higgsblock.global.chain.network.socket.event;

import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Data
public class ReceivedMessageEvent extends BaseSerializer {
    private String channelId;
    private String content;
}
