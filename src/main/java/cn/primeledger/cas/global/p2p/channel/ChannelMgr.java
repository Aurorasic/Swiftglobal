package cn.primeledger.cas.global.p2p.channel;

import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.p2p.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The channel manager holds all channels. Also which manages the channel's state and count.
 *
 * @author zhao xiaogang
 * */
public class ChannelMgr {
    private static final Logger logger = LoggerFactory.getLogger(ChannelMgr.class);

    private ConcurrentHashMap<InetSocketAddress, Channel> allChannels;
    private ConcurrentHashMap<Long, Channel> activeChannels;

    public Network network;

    public ChannelMgr(Network network) {
        this.network = network;
        this.allChannels = new ConcurrentHashMap<>();
        this.activeChannels = new ConcurrentHashMap<>();
    }

    /** Add channel to the channel map.*/
    public void add(Channel channel) {
        logger.debug("The added peer address is {}:{}", channel.getPeerAddress(), channel.getPeerPort());

        allChannels.put(channel.getPeerAddress(), channel);
    }

    /** Remove channel from the channel map.*/
    public void remove(Channel channel) {
        activeChannels.remove(channel.getPeerAddress());

        if (channel.isActive()) {
            activeChannels.remove(channel.getId());
            channel.deactive();
        }
    }

    /**
     * If channel is active, we need change the channel state to be active and put the
     * channel to the active channels' concurrent map.
     * */
    public void onChanneActive(Channel channel) {
        channel.onActive();
        activeChannels.put(channel.getId(), channel);
    }

    /**Get the channel count*/
    public int getChannelCount() {
        return allChannels.size();
    }

    /**Get the active peers.*/
    public List<Peer> getActivePeers() {
        List<Peer> peerList = new ArrayList<>();

        for (Channel channel : activeChannels.values()) {
            peerList.add(channel.getPeerNode());
        }

        return peerList;
    }

    /**Get the active addresses.*/
    public List<InetSocketAddress> getActiveAddresses() {
        List<InetSocketAddress> addressList = new ArrayList<>();

        for (Channel channel : activeChannels.values()) {
            Peer peer = channel.getPeerNode();
            addressList.add(new InetSocketAddress(peer.getIp(), peer.getPort()));
        }

        return addressList;
    }
}
