package cn.primeledger.cas.global.p2p.channel;

import cn.primeledger.cas.global.config.AppConfig;
import cn.primeledger.cas.global.p2p.Peer;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The channel manager holds all channels. which manages the all channels' state and count. It has
 * two channel list. One is all channel list, which contains the channel before handshaking with
 * ping message.The other one is active channel, which contains the channel after handshaking with
 * ping message.
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class ChannelMgr {

    private final static int TIME_WAIT = 30;
    private static final int LRU_CACHE_SIZE = 1000;

    @Autowired
    private AppConfig config;

    private Map<String, Channel> channelMap;

    private Cache<String, Long> cache = Caffeine.newBuilder().maximumSize(LRU_CACHE_SIZE)
            .expireAfterAccess(TIME_WAIT, TimeUnit.SECONDS)
            .build();

    public ChannelMgr() {
        this.channelMap = Maps.newConcurrentMap();
    }

    /**
     * Add channel to the channel map.
     */
    public void add(Channel channel) {
        LOGGER.info("add channel,id = {}", channel.getId());
        channelMap.put(channel.getId(), channel);

        // todo baizhengwen 删除未激活的连接
    }

    /**
     * Remove channel from the channel map.
     */
    public void remove(Channel channel) {
        LOGGER.info("Removed channel, id = {}", channel.getId());
        channelMap.remove(channel.getId());

        // todo baizhengwen 删除未激活的连接
    }

    public boolean isConnected(String peerId) {
        // todo baizhengwen
        return getActiveChannels().stream().anyMatch(channel -> StringUtils.equals(channel.getPeerId(), peerId));
    }

    /**
     * Get the channel count
     */
    public int countAllChannels() {
        return channelMap.size();
    }

    public int countActionChannels() {
        return getActiveChannels().size();
    }

    public boolean canConnect() {
        return countAllChannels() < config.getMaxInboundConnections();
    }

    /**
     * Get the active peers.
     */
    public List<Peer> getActivePeers() {
        return getActiveChannels().stream().map(Channel::getPeer).collect(Collectors.toList());
    }

    /**
     * Get the active channels.
     */
    public List<Channel> getActiveChannels() {
        return channelMap.values().stream().filter(channel -> channel.isActivated()).collect(Collectors.toList());
    }

    public Channel getChannelById(String id) {
        return channelMap.get(id);
    }

    public Channel getChannelByPeerId(String peerId) {
        return getActiveChannels().stream().filter(channel -> StringUtils.equals(peerId, channel.getPeerId())).findFirst().orElse(null);
    }

    public boolean shouldDispatch(String data, String sourceId) {
        LOGGER.info("data :{}  sourceId:{}", data, sourceId);
        String key = Hashing.goodFastHash(128)
                .hashString(data, Charsets.UTF_8).toString();

        Long lastTime = cache.getIfPresent(key + ":" + sourceId);

        return (lastTime == null);
    }

    public void putMessageCached(String msg, String sourceId) {
        String key = Hashing.goodFastHash(128).hashString(msg, Charsets.UTF_8).toString();
        long now = System.currentTimeMillis();
        cache.put(key + ":" + sourceId, now);
    }
}
