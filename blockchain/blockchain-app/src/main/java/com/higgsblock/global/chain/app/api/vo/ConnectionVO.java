package com.higgsblock.global.chain.app.api.vo;

import com.higgsblock.global.chain.app.net.constants.ConnectionLevelEnum;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.network.socket.constants.ChannelType;
import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/4/9
 */
@Data
public class ConnectionVO extends BaseSerializer {
    private String channelId;
    private String peerId;
    private String ip;
    private Integer port;
    private String age;
    private Boolean activated;
    private ChannelType type;
    private ConnectionLevelEnum connectionLevel;
}
