package com.higgsblock.global.chain.network.discover;

import com.higgsblock.global.chain.network.config.PeerConfig;
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
@Component("localDiscovery")
public class LocalPeerConnectionInfoDiscovery implements IPeerConnectionInfoDiscovery {

    /**
     * The Peer config.
     */
    @Autowired
    private PeerConfig peerConfig;

    @Override
    public String getIp() {
        return peerConfig.getIp();
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
