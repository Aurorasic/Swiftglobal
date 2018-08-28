package com.higgsblock.global.chain.app.task;

import com.higgsblock.global.chain.app.config.AppConfig;
import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import com.higgsblock.global.chain.network.config.PeerConfig;
import com.higgsblock.global.chain.network.discover.IPeerConnectionInfoDiscovery;
import com.higgsblock.global.chain.network.enums.NetworkType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * The type Inet address check task.
 *
 * @author baizhengwen
 * @date 2018 /3/22
 */
@Component
public class InetAddressCheckTask extends BaseTask {

    @Autowired
    private AppConfig appConfig;
    /**
     * The Config.
     */
    @Autowired
    private PeerConfig peerConfig;
    /**
     * The Local discovery.
     */
    @Autowired
    @Qualifier("localDiscovery")
    private IPeerConnectionInfoDiscovery localDiscovery;
    /**
     * The Public discovery.
     */
    @Autowired
    @Qualifier("publicDiscovery")
    private IPeerConnectionInfoDiscovery publicDiscovery;
    /**
     * The Peer manager.
     */
    @Autowired
    private PeerManager peerManager;

    @Override
    protected void task() {
        if (NetworkType.DEV_NET.getType() == appConfig.getNetworkType()) {
            updatePeerInfo(localDiscovery.getIp(), localDiscovery.getSocketPort(), localDiscovery.getHttpPort());
        } else {
            updatePeerInfo(publicDiscovery.getIp(), publicDiscovery.getSocketPort(), publicDiscovery.getHttpPort());
        }

        peerManager.reportAndGetPeers();
    }

    @Override
    protected long getPeriodMs() {
        return TimeUnit.HOURS.toMillis(1);
    }

    /**
     * Update peer info.
     *
     * @param ip         the ip
     * @param socketPort the socket port
     * @param httpPort   the http port
     */
    private void updatePeerInfo(String ip, int socketPort, int httpPort) {
        Peer peer = new Peer();
        peer.setIp(ip);
        peer.setSocketServerPort(socketPort);
        peer.setHttpServerPort(httpPort);
        peer.setPubKey(peerConfig.getPubKey());
        peer.signature(peerConfig.getPriKey());
        peerManager.setSelf(peer);
    }
}