package com.higgsblock.global.chain.app.api.vo;

import com.higgsblock.global.chain.app.net.constants.ConnectionLevelEnum;
import com.higgsblock.global.chain.network.socket.constants.ChannelType;
import lombok.Data;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author baizhengwen
 * @date 2018/4/9
 */
@Data
public class ConnectionVO {

    private String channelId;
    private String peerId;
    private String ip;
    private int port;
    private boolean isActivated;
    private ChannelType type;
    private ConnectionLevelEnum connectionLevel;

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        return builder.append("channelId", channelId)
                .append("peerId", peerId)
                .append("ip", ip)
                .append("port", port)
                .append("type", type)
                .append("isActivated", isActivated)
                .append("connectionLevel", connectionLevel)
                .toString();
    }
}
