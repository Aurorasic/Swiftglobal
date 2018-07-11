package com.higgsblock.global.chain.app.discover;

import com.higgsblock.global.chain.network.config.PeerConfig;
import com.higgsblock.global.chain.network.utils.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The type Local peer connection info discovery.
 *
 * @author zhao xiaogang
 * @date 2018 /4/11
 */
@Slf4j
@Component
public class LocalPeerConnectionInfoDiscovery implements IPeerConnectionInfoDiscovery {

    /**
     * The Peer config.
     */
    @Autowired
    private PeerConfig peerConfig;

    @Override
    public String getIp() {
        return NetworkUtil.getIpByName(peerConfig.getIp());
    }

    @Override
    public int getSocketPort() {
        return peerConfig.getSocketPort();
    }

    @Override
    public int getHttpPort() {
        return peerConfig.getHttpPort();
    }
}
