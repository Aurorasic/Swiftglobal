package cn.primeledger.cas.global.p2p;

import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import cn.primeledger.cas.global.p2p.discover.UpnpMgr;

/**
 * Network manager is responsible for globe management of the p2p network.
 *
 * @author zhao xiaogang
 */
public class NetworkMgr {
    private volatile boolean isRunning;
    private Network network;

    private PeerClient p2PClient;
    private PeerServer p2PServer;

    private ChannelMgr channelMgr;
    private PeerMgr peerMgr;
    private UpnpMgr upnpMgr;

    public NetworkMgr(Network network) {
        this.network = network;
    }

    public synchronized void start() {
        if (!isRunning) {
            p2PClient = new PeerClient(this);
            p2PServer = new PeerServer(this);

            channelMgr = new ChannelMgr(network);
            peerMgr = new PeerMgr(this);
            upnpMgr = new UpnpMgr(network);

            p2PServer.start();
            peerMgr.start();
            upnpMgr.start();

            isRunning = true;
        }
    }

    public synchronized void shutdown() {
        if (isRunning) {
            peerMgr.shutdown();
            p2PServer.shutdown();
            upnpMgr.shutdown();

            isRunning = false;
        }
    }

    public Network getNetwork() {
        return network;
    }

    public ChannelMgr getChannelMgr() {
        return channelMgr;
    }

    public PeerClient getP2PClient() {
        return p2PClient;
    }

    public PeerMgr getPeerMgr() {
        return peerMgr;
    }
}
