package cn.primeledger.cas.global.p2p.discover;

import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.p2p.NetworkMgr;
import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.p2p.PeerClient;
import cn.primeledger.cas.global.p2p.channel.ChannelInitializer;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.List;

/**
 * Peer connector is responsible for connecting to the peer nodes for p2p communication.
 *
 * @author zhao xiaogang
 */
public class PeerConnector implements Runnable {
    private final static int RECONNECT_WAIT = 60 * 1000;
    private static final int LRU_CACHE_SIZE = 100;

    private Cache<Peer, Long> peerCacheMap = Caffeine.newBuilder().
            maximumSize(LRU_CACHE_SIZE).build();

    private NetworkMgr networkMgr;
    private ChannelMgr channelMgr;
    private Network network;
    private PeerClient p2PClient;
    private Deque<Peer> peerDeque;

    public PeerConnector(NetworkMgr networkMgr) {
        this.networkMgr = networkMgr;
        this.channelMgr = networkMgr.getChannelMgr();
        this.network = networkMgr.getNetwork();
        this.p2PClient = networkMgr.getP2PClient();
        this.peerDeque = networkMgr.getPeerMgr().getPeers();
    }

    @Override
    public void run() {
        connect();
    }

    private void connect() {
        List<InetSocketAddress> activeAddresses = channelMgr.getActiveAddresses();
        Peer peerNode;

        while ((peerNode = peerDeque.pollFirst()) != null &&
                channelMgr.getChannelCount() < network.maxOutboundConnections()) {
            Long lastTime = peerCacheMap.getIfPresent(peerNode);
            long now = System.currentTimeMillis();

            if (!checkConnect(activeAddresses, peerNode, lastTime, now)) {
                ChannelInitializer initializer = new ChannelInitializer(networkMgr, peerNode);
                p2PClient.connect(peerNode, initializer);
                peerCacheMap.put(peerNode, now);
                break;
            }
        }
    }

    private boolean checkConnect(List<InetSocketAddress> activeAddresses, Peer peerNode, Long lastTime, long now) {
        return p2PClient.getSelf().equals(peerNode)
                && !activeAddresses.contains(peerNode.getAddress())
                && (lastTime == null || lastTime + RECONNECT_WAIT < now);
    }

}
