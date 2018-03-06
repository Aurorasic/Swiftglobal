package cn.primeledger.cas.global.p2p.channel;

import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.p2p.listener.SendMessageListener;
import cn.primeledger.cas.global.p2p.message.GetPeersMessage;
import cn.primeledger.cas.global.p2p.message.PingMessage;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The channel manager holds all channels. which manages the all channels' state and count. It has
 * two channel list. One is all channel list, which contains the channel before handshaking with
 * ping message.The other one is active channel, which contains the channel after handshaking with
 * ping message. Moreover, it can start{@link ChannelMgr#start} or shut down{@link ChannelMgr#shutdown}
 * a ping timer.
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class ChannelMgr implements InitializingBean {
    private final static int PING_DELAY = 15 * 1000;
    private final static int PING_PERIOD = 60 * 1000;

    private final static int TIME_WAIT = 60 * 1000;
    private static final int LRU_CACHE_SIZE = 1000;

    private ConcurrentHashMap<InetSocketAddress, Channel> allChannels;
    private ConcurrentHashMap<Long, Channel> activeChannels;

    private ScheduledExecutorService executorService;
    private Cache<String, Long> cache = Caffeine.newBuilder().maximumSize(LRU_CACHE_SIZE).build();
    //public Network network;

//    public ChannelMgr(Network network) {
//        this.network = network;
//        this.allChannels = new ConcurrentHashMap<>();
//        this.activeChannels = new ConcurrentHashMap<>();
//
//        AtomicInteger atomicInteger = new AtomicInteger(1);
//        this.executorService = Executors.newSingleThreadScheduledExecutor(r -> {
//            return new Thread(r, "PING-TIMER-" + atomicInteger.getAndIncrement());
//        });
//
////        SendMessageListener listener = network.context().getBean(SendMessageListener.class);
////        listener.setChannelMgr(this);
//    }

    /**
     * Start the ping timer which send ping message to the peer node at time.
     */
    public void start() {
        this.executorService.scheduleAtFixedRate(() -> {
            for (Channel channel : activeChannels.values()) {
                channel.getMessageQueue().sendMessage(new PingMessage());
                channel.getMessageQueue().sendMessage(new GetPeersMessage());
            }
        }, PING_DELAY, PING_PERIOD, TimeUnit.MILLISECONDS);
    }

    /**
     * Add channel to the channel map.
     */
    public void add(Channel channel) {
        LOGGER.info("The added peer address is {}", channel.getPeerAddress());
        allChannels.put(channel.getPeerAddress(), channel);
    }

    /**
     * Remove channel from the channel map.
     */
    public void remove(Channel channel) {
        LOGGER.info("Removed channel id is: {}", channel.getId());
        allChannels.remove(channel.getPeerAddress());

        if (channel.isActive()) {
            activeChannels.remove(channel.getId());
            channel.deactive();
        }
    }

    /**
     * If the client finish to shake with the peer, we need change the channel state to
     * be active and put the channel to the active channels' concurrent map.
     */
    public void onChanneActive(Channel channel, Peer peer) {
        channel.onActive(peer);
        activeChannels.put(channel.getId(), channel);
    }

    /**
     * Get the channel count
     */
    public int getChannelCount() {
        return allChannels.size();
    }

    public int getDelegateConnections() {
        int count = 0;
        for (Channel channel : activeChannels.values()) {
            if (channel.isDelegate()) {
                count ++;
            }
        }
        return count;
    }

    /**
     * Get the active peers.
     */
    public List<Peer> getActivePeers() {
        List<Peer> peerList = new ArrayList<>();

        for (Channel channel : activeChannels.values()) {
            peerList.add(channel.getPeerNode());
        }

        return peerList;
    }

    /**
     * Get the all peer addresses.
     */
    public List<InetSocketAddress> getAllAddresses() {
        List<InetSocketAddress> addressList = new ArrayList<>();

        for (Channel channel : allChannels.values()) {
            Peer peer = channel.getPeerNode();
            addressList.add(new InetSocketAddress(peer.getIp(), peer.getPort()));
        }

        return addressList;
    }

    /**
     * Get the active channels.
     */
    public List<Channel> getActiveChannels() {
        List<Channel> list = new ArrayList<>();
        list.addAll(activeChannels.values());
        return list;
    }

    /**
     * Get the active addresses that all must be the public addresses.
     */
    public List<InetSocketAddress> getActiveAddresses() {
        List<InetSocketAddress> addressList = new ArrayList<>();

        for (Channel channel : activeChannels.values()) {
            Peer peer = channel.getPeerNode();
            addressList.add(new InetSocketAddress(peer.getIp(), peer.getPort()));
        }

        return addressList;
    }

    public Channel getChannelById(long id) {
        Channel channel = null;
        for (Channel ch : activeChannels.values()) {
            if (ch.getId() == id) {
                channel = ch;
                break;
            }
        }

        return channel;
    }

    /**
     * Returns whether the specified IP address is connected.
     */
    public boolean isConnected(String ip) {
        for (Channel c : activeChannels.values()) {
            if (c.getPeerAddress().equals(ip)) {
                return true;
            }
        }

        return false;
    }

    public boolean shouldDispatch(String data) {
        String key = Hashing.goodFastHash(128)
                .hashString(data, Charsets.UTF_8).toString();

        long now = System.currentTimeMillis();
        Long lastTime = cache.getIfPresent(key);

        return (lastTime == null || (lastTime + TIME_WAIT < now));
    }

    public void putMessageCached(String msg) {
        String key = Hashing.goodFastHash(128).hashString(msg, Charsets.UTF_8).toString();
        long now = System.currentTimeMillis();
        cache.put(key, now);
    }

    /**
     * Shut down the ping timer.
     */
    public void shutdown() {
        this.executorService.shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.allChannels = new ConcurrentHashMap<>();
        this.activeChannels = new ConcurrentHashMap<>();

        AtomicInteger atomicInteger = new AtomicInteger(1);
        this.executorService = Executors.newSingleThreadScheduledExecutor(r -> {
            return new Thread(r, "PING-TIMER-" + atomicInteger.getAndIncrement());
        });
    }
}
