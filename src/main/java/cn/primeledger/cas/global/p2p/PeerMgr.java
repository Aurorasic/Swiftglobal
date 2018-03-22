package cn.primeledger.cas.global.p2p;

import cn.primeledger.cas.global.p2p.channel.Channel;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import cn.primeledger.cas.global.p2p.message.GetPeersMessage;
import cn.primeledger.cas.global.service.PeerReqService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The peer manager holds the all the peer nodes information. Which will refresh the peer nodes
 * from the dns seeds at time. And it will connect to the peer nodes at a fix rate time.
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class PeerMgr {
    private final static int MAX_QUEUE_SIZE = 100;

    private final static int CONNECTOR_DELAY = 100;
    private final static int CONNECTOR_PERIOD = 500;

    private final static int MESSAGE_DELAY = 5;
    private final static int MESSAGE_PERIOD = 15;

    private final static int RECONNECT_WAIT = 60 * 1000;
    private static final int LRU_CACHE_SIZE = 100;


    private volatile boolean isRunning;

    private Cache<Peer, Long> peerCacheMap = Caffeine.newBuilder().
            maximumSize(LRU_CACHE_SIZE).build();

    private Deque<Peer> peersDeque;
    private ScheduledExecutorService executorService;

    @Autowired
    public ChannelMgr channelMgr;

    @Autowired
    private PeerClient peerClient;

    @Autowired
    private PeerReqService peerReqService;

    private ScheduledFuture connectorFuture;
    private ScheduledFuture messageFuture;

    public PeerMgr() {
        this.peersDeque = new ConcurrentLinkedDeque<>();

        this.executorService = Executors.newScheduledThreadPool(2, new ThreadFactory() {
            AtomicInteger atomicInteger = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "P2P-MGR-" + atomicInteger.getAndIncrement());
            }
        });
    }

    /**
     * add peer node to the peers queue
     */
    public void addPeer(Peer peer) {
        peersDeque.addFirst(peer);
        while (peersDeque.size() > MAX_QUEUE_SIZE) {
            peersDeque.removeLast();
        }
    }

    /**
     * Return the management queue of the peers.
     */
    public Deque<Peer> getPeers() {
        return peersDeque;
    }

    /**
     * Start tasks to get peer nodes and then connect with them for p2p communication.
     */
    public synchronized void start() {
        if (!isRunning) {
            connectorFuture = executorService.scheduleAtFixedRate(
                    this::connect,
                    CONNECTOR_DELAY,
                    CONNECTOR_PERIOD,
                    TimeUnit.MILLISECONDS);

            messageFuture = executorService.scheduleAtFixedRate(
                    this::fetchPeers,
                    MESSAGE_DELAY,
                    MESSAGE_PERIOD,
                    TimeUnit.SECONDS);

            isRunning = true;
            LOGGER.info("The peer manager started");

        }
    }

    /**
     * Shutdown the get peer node and connector tasks.
     */
    public synchronized void shutdown() {
        if (isRunning) {
            connectorFuture.cancel(true);
            messageFuture.cancel(true);

            isRunning = false;

            LOGGER.info("The peer manager shut down");
        }
    }

    /**
     * Add peers to the peer queue.
     */
    public void add(Collection<Peer> collection) {
        CollectionUtils.forAllDo(collection, o -> addPeer((Peer) o));
    }

    public void doGetSeedPeers() {
        List<Peer> peers = peerReqService.doGetSeedPeersRequest();

        LOGGER.info("get peers: {}", peers);
        if (CollectionUtils.isNotEmpty(peers)) {
            add(peers);
        }
    }

    private void connect() {
        Peer peerNode;
        while ((peerNode = peersDeque.pollFirst()) != null) {
            long now = System.currentTimeMillis();
            peerClient.connect(peerNode);
//            peerCacheMap.put(peerNode, now);
        }
    }

    /**
     * Send get peers message to get more peer
     */
    private void fetchPeers() {
        List<Channel> channels = channelMgr.getActiveChannels();
        for (Channel channel : channels) {
            channel.sendMessage(new GetPeersMessage());
        }
    }
}


