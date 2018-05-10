package com.higgsblock.global.chain.app.schedule;

import com.higgsblock.global.chain.app.config.AppConfig;
import com.higgsblock.global.chain.app.config.NetworkType;
import com.higgsblock.global.chain.app.discover.LocalPeerConnectionInfoDiscovery;
import com.higgsblock.global.chain.app.discover.PublicPeerConnectionInfoDiscovery;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @date 2018/3/22
 */
//@Component
public class InetAddressCheckTask extends BaseTask {

    @Autowired
    private AppConfig config;
    @Autowired
    private LocalPeerConnectionInfoDiscovery localDiscovery;
    @Autowired
    private PublicPeerConnectionInfoDiscovery publicDiscovery;
    @Autowired
    private PeerManager peerManager;

    @Override
    protected void task() {
        if (NetworkType.DEVNET.getType() == config.getNetworkType()) {
            updatePeerInfo(localDiscovery.getIp(), localDiscovery.getSocketPort(), localDiscovery.getHttpPort());
        } else {
            updatePeerInfo(publicDiscovery.getIp(), publicDiscovery.getSocketPort(), publicDiscovery.getHttpPort());
        }
    }

    @Override
    protected long getPeriodMs() {
        return TimeUnit.HOURS.toMillis(1);
    }

    private void updatePeerInfo(String ip, int socketPort, int httpPort) {
        Peer peer = new Peer();
        peer.setIp(ip);
        peer.setSocketServerPort(socketPort);
        peer.setHttpServerPort(httpPort);
        peer.setPubKey(config.getPubKey());
        peer.signature(config.getPriKey());

        peerManager.setSelf(peer);
    }
}
