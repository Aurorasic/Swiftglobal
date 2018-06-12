package com.higgsblock.global.chain.network.config;

import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018-05-02
 */
@Data
public class RegistryConfig {
    private String ip;
    private int port;

    @Override
    public String toString() {
        return ip + ":" + port;
    }
}
