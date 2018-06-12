package com.higgsblock.global.chain.app.discover;

import com.higgsblock.global.chain.app.config.AppConfig;
import com.higgsblock.global.chain.network.utils.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhao xiaogang
 * @date 2018/4/11
 */
@Slf4j
@Component
public class LocalPeerConnectionInfoDiscovery implements IPeerConnectionInfoDiscovery {

    @Autowired
    private AppConfig config;

    @Override
    public String getIp() {
        return NetworkUtil.getLocalIp();
    }

    @Override
    public int getSocketPort() {
        return config.getSocketServerPort();
    }

    @Override
    public int getHttpPort() {
        return config.getHttpServerPort();
    }
}
