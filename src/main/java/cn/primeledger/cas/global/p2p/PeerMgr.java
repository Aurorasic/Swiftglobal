package cn.primeledger.cas.global.p2p;

import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import cn.primeledger.cas.global.p2p.discover.MessageDiscover;
import cn.primeledger.cas.global.p2p.discover.PeerConnector;
import cn.primeledger.cas.global.p2p.store.PeerStoreTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The peer manager holds the all the peer nodes information. Which will refresh the peer nodes from
 * the dns seeds at time. And it will connect to the peer nodes at a fix rate time.
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class PeerMgr implements InitializingBean {
    private final static int MAX_QUEUE_SIZE = 100;

    private final static int DISCOVER_DELAY = 2;
    private final static int DISCOVER_PERIOD = 5;

    private final static int CONNECTOR_DELAY = 150;
    private final static int CONNECTOR_PERIOD = 400;

    private final static int PEERSTORE_DELAY = 60;
    private final static int PEERSTORE_PERIOD = 200;

    private final static int MESSAGE_DELAY = 15;
    private final static int MESSAGE_PERIOD = 60;

    private volatile boolean isRunning;
    private volatile boolean isChannelInit;

    private Deque<Peer> peersDeque;
    private ScheduledExecutorService executorService;

    @Autowired
    private NetworkMgr networkMgr;

    @Autowired
    public ChannelMgr channelMgr;


    //private ScheduledFuture discoverFuture;
    private ScheduledFuture connectorFuture;
    private ScheduledFuture peerStoreFuture;
    private ScheduledFuture messageFuture;

    /**
     * add peer node to the peers queue
     */
    public void addPeer(Peer peer) {
        peersDeque.addFirst(peer);
        while (peersDeque.size() > MAX_QUEUE_SIZE) {
            peersDeque.removeLast();
        }

        //LOGGER.info("Peers queue size: {}, {}", peersDeque, peersDeque.size());
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
        }
    }

    /**
     * Start tasks to get peer nodes and then connect with them for p2p communication.
     */
    public synchronized void start() {
        if (!isRunning) {
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

            messageFuture = executorService.scheduleAtFixedRate(
                    new MessageDiscover(channelMgr),
                    MESSAGE_DELAY,
                    MESSAGE_PERIOD,
                    TimeUnit.SECONDS);

            isRunning = true;
            LOGGER.info("The peer manager started");
        }

        addPeer(new Peer("192.168.193.13", networkMgr.getNetwork().p2pServerListeningPort()));
    }

    /**
     * Shutdown the get peer node and connector tasks.
     */
    public synchronized void shutdown() {
        if (isRunning) {
            //discoverFuture.cancel(false);
            connectorFuture.cancel(true);
            peerStoreFuture.cancel(false);
            messageFuture.cancel(true);

            isRunning = false;

            LOGGER.info("The peer manager shut down");
        }
    }

    public void add(Collection<Peer> collection) {
        CollectionUtils.forAllDo(collection, o -> addPeer((Peer) o));
    }

    public void addPeers(Collection<String> addressList) {
        CollectionUtils.forAllDo(addressList, o -> addPeer(new Peer((String) o,
                networkMgr.getNetwork().p2pServerListeningPort())));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.peersDeque = new ConcurrentLinkedDeque<>();
        this.executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            AtomicInteger atomicInteger = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "P2P-MGR-" + atomicInteger.getAndIncrement());
            }
        });
    }
}

