package cn.primeledger.cas.global.p2p;

import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import cn.primeledger.cas.global.p2p.discover.DnsDiscovery;
import cn.primeledger.cas.global.p2p.discover.PeerConnector;
import cn.primeledger.cas.global.p2p.store.PeerDatabase;
import cn.primeledger.cas.global.p2p.store.PeerStoreTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The peer manager holds the all the peer nodes information. Which will refresh the peer nodes from
 * the dns seeds at time. And it will connect to the peer nodes at a fix rate time.
 *
 * @author zhao xiaogang
 */
public class PeerMgr {
    private static final Logger logger = LoggerFactory.getLogger(PeerMgr.class);

    private final static int MAX_QUEUE_SIZE = 100;

    private final static int DISCOVER_DELAY = 2;
    private final static int DISCOVER_PERIOD = 200;

    private final static int CONNECTOR_DELAY = 150;
    private final static int CONNECTOR_PERIOD = 400;

    private final static int PEERSTORE_DELAY = 60;
    private final static int PEERSTORE_PERIOD = 200;

    private volatile boolean isRunning;
    private volatile boolean isChannelInit;

    private Deque<Peer> peersDeque;
    private ScheduledExecutorService executorService;

    private NetworkMgr networkMgr;
    public ChannelMgr channelMgr;
    public Network network;
    public PeerClient p2PClient;


    private ScheduledFuture discoverFuture;
    private ScheduledFuture connectorFuture;
    private ScheduledFuture peerStoreFuture;

    public PeerMgr(NetworkMgr networkMgr) {
        this.networkMgr = networkMgr;
        this.network = networkMgr.getNetwork();
        this.channelMgr = networkMgr.getChannelMgr();
        this.p2PClient = networkMgr.getP2PClient();

        this.peersDeque = new ConcurrentLinkedDeque<>();
        this.executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            AtomicInteger atomicInteger = new AtomicInteger(0);

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
            peersDeque.remove();
        }
    }

    /**
     * Return the management queue of the peers.
     */
    public Deque<Peer> getPeers() {
        return peersDeque;
    }

    /**
     * Return the Peer manager's running state.
     */
    public boolean isActive() {
        return isRunning;
    }

    /**
     * Return the peer count.
     */
    public int getPeerCount() {
        return peersDeque.size();
    }

    public boolean isChannelInit() {
        return isChannelInit;
    }

    /***
     * At least one channel is active, {@link PeerMgr#onChannelActive} will be invoked.
     */
    public void onChannelActive(boolean channelActive) {
        if (!isChannelInit) {
            isChannelInit = true;

            //Persist peers to database
            if (network.peerPersistEnabled()) {

            }
        }
    }

    /**
     * Start tasks to get peer nodes and then connect with them for p2p communication.
     */
    public synchronized void start() {
        if (!isRunning) {
            discoverFuture = executorService.scheduleAtFixedRate(
                    new DnsDiscovery(this),
                    DISCOVER_DELAY,
                    DISCOVER_PERIOD,
                    TimeUnit.SECONDS);

            connectorFuture = executorService.scheduleAtFixedRate(
                    new PeerConnector(networkMgr),
                    CONNECTOR_DELAY,
                    CONNECTOR_PERIOD,
                    TimeUnit.MILLISECONDS);

            peerStoreFuture = executorService.scheduleAtFixedRate(
                    new PeerStoreTask(this),
                    PEERSTORE_DELAY,
                    PEERSTORE_PERIOD,
                    TimeUnit.SECONDS);

            isRunning = true;
            logger.info("The peer manager started");
        }
    }

    /**
     * Shutdown the get peer node and connector tasks.
     */
    public synchronized void shutdown() {
        if (isRunning) {
            discoverFuture.cancel(false);
            connectorFuture.cancel(true);
            peerStoreFuture.cancel(false);

            isRunning = false;

            logger.info("The peer manager stopped");
        }
    }
}

