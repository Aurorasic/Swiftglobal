package com.higgsblock.global.chain.network.config;

import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018-07-23
 */
@Data
public class SocketConfig {
    private int serverPort;
    private int connectionTimeOutMs;

    private int channelLimitIn;
    private int channelLimitOut;
}
