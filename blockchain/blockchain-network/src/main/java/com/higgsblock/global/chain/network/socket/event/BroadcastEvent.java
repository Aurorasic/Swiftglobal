package com.higgsblock.global.chain.network.socket.event;

import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Data
public class BroadcastEvent extends MessageEvent {

    private String[] excludeSourceIds;

}
