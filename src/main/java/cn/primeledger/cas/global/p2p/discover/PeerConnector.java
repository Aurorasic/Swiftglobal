//package cn.primeledger.cas.global.p2p.discover;
//
//import cn.primeledger.cas.global.config.Network;
//import cn.primeledger.cas.global.p2p.NetworkMgr;
//import cn.primeledger.cas.global.p2p.Peer;
//import cn.primeledger.cas.global.p2p.PeerClient;
//import cn.primeledger.cas.global.p2p.PeerMgr;
//import cn.primeledger.cas.global.p2p.channel.ChannelInitializer;
//import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
//import com.alibaba.fastjson.JSON;
//import com.github.benmanes.caffeine.cache.Cache;
//import com.github.benmanes.caffeine.cache.Caffeine;
//import lombok.extern.slf4j.Slf4j;
//
//import java.net.InetSocketAddress;
//import java.util.Deque;
//import java.util.List;
//
///**
// * Peer connector is responsible for connecting to the peer nodes for p2p communication.
// *
// * @author zhao xiaogang
// */
//
//@Slf4j
//public class PeerConnector implements Runnable {
//    private final static int RECONNECT_WAIT = 60 * 1000;
//    private static final int LRU_CACHE_SIZE = 100;
//
//    private Cache<Peer, Long> peerCacheMap = Caffeine.newBuilder().
//            maximumSize(LRU_CACHE_SIZE).build();
//
//    private NetworkMgr networkMgr;
//    private ChannelMgr channelMgr;
//    private Network network;
//    private PeerClient p2PClient;
//    private Deque<Peer> peerDeque;
//
//    public PeerConnector(NetworkMgr networkMgr, PeerMgr peerMgr, ChannelMgr channelMgr) {
//        this.networkMgr = networkMgr;
//        this.network = networkMgr.getNetwork();
//        this.peerDeque = peerMgr.getPeers();
//        this.channelMgr = channelMgr;
//        this.p2PClient = networkMgr.getNetwork().context().getBean(PeerClient.class);
//    }
//
//    @Override
//    public void run() {
//        //LOGGER.info("Peer connector is running...");
//        connect();
//    }
//
//    private void connect() {
//        List<InetSocketAddress> allAddresses = channelMgr.getAllAddresses();
//        Peer peerNode;
//
//        //LOGGER.warn("peer queue: {}", JSON.toJSONString(peerDeque));
//        while ((peerNode = peerDeque.pollFirst()) != null) {
//
//            LOGGER.info("Current connecting peer node : {}", JSON.toJSONString(peerNode));
//            Long lastTime = peerCacheMap.getIfPresent(peerNode);
//            long now = System.currentTimeMillis();
//
//            if (isValid(peerNode) && notConnected(allAddresses, peerNode, lastTime, now)) {
//                ChannelInitializer initializer = new ChannelInitializer(networkMgr, peerNode);
//                p2PClient.connect(peerNode, initializer);
//                peerCacheMap.put(peerNode, now);
//            }
//        }
//    }
//
//    /**
//     * Check the peer connections whether exceed the max.
//     */
//    private boolean isValid(Peer peerNode) {
//        int directChannelCount = channelMgr.getDirectChannelCount();
//        int p2pChannelCount = channelMgr.count() - directChannelCount;
//
//        return peerNode.isDelegate() ? (directChannelCount < network.maxDelegatePeerCount()) :
//                (p2pChannelCount < network.maxOutboundConnections());
//    }
//
//    /**
//     * Check if the peer's address not equal the local address, the all addresses not contain
//     * the peer's address, and last cache time equals null or the waiting time is not bigger
//     * than the default value. These three conditions determine whether should connect to peers.
//     * */
//    private boolean notConnected(List<InetSocketAddress> allAddresses, Peer peerNode, Long lastTime, long now) {
//        InetSocketAddress address = new InetSocketAddress(peerNode.getIp(), peerNode.getPort());
//        return !p2PClient.getSelf().equals(peerNode) && !allAddresses.contains(address)
//                && (lastTime == null || (lastTime + RECONNECT_WAIT < now));
//    }
//}
