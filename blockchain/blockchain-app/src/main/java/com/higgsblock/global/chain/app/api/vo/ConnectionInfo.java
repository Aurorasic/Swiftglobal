package com.higgsblock.global.chain.app.api.vo;

import lombok.Data;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author baizhengwen
 * @date 2018/4/9
 */
@Data
public class ConnectionInfo {

    private String id;
    private String peerId;
    private String ip;
    private int port;
    private boolean isActivated;
    private boolean isClient;

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        return builder.append("id", id)
                .append("peerId", peerId)
                .append("ip", ip)
                .append("port", port)
                .append("isClient", isClient)
                .append("isActivated", isActivated)
                .toString();
    }
}
