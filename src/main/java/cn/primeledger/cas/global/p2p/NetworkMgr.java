package cn.primeledger.cas.global.p2p;

import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import cn.primeledger.cas.global.p2p.discover.UpnpMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Network manager is responsible for global management of the p2p network.
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class NetworkMgr {
    private volatile boolean isRunning;

    private Network network;

    @Autowired
    private PeerClient p2PClient;

    @Autowired
    private PeerServer p2PServer;

    @Autowired
    private ChannelMgr channelMgr;

    @Autowired
    private PeerMgr peerMgr;

    @Autowired
    private UpnpMgr upnpMgr;

    @Autowired
    private RegisterCenter registerCenter;

    public synchronized void start() {
        LOGGER.info("NetworkMgr started");
        if (!isRunning) {
            channelMgr.start();
            p2PServer.start();
            p2PClient.start();

            peerMgr.start();
            upnpMgr.start();
            registerCenter.sendRegistryMessage();
            isRunning = true;
        }
    }

    public synchronized void shutdown() {
        if (isRunning) {
            channelMgr.shutdown();
            peerMgr.shutdown();
            p2PServer.shutdown();
            upnpMgr.shutdown();

            isRunning = false;
            LOGGER.info("NetworkMgr shut down");
        }
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Network getNetwork() {
        return network;
    }

}
