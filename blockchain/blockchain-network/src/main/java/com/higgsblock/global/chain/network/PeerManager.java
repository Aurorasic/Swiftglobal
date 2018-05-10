package com.higgsblock.global.chain.network;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.network.api.IRegistryApi;
import com.higgsblock.global.chain.network.config.PeerConfig;
import com.higgsblock.global.chain.network.socket.PeerCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * The peer manager provides basic methods to add peers into database or to find peers from database.
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class PeerManager implements InitializingBean {

    @Autowired
    private ConcurrentMap<String, Peer> peerMap;

    @Autowired
    private PeerConfig config;

    @Autowired
    private PeerCache peerCache;

    @Autowired
    private IRegistryApi registryApi;

    private Peer self;

    @Override
    public void afterPropertiesSet() throws Exception {
        // todo baizhengwen 移动到启动逻辑中
        getSelf();
    }

    /**
     * Add peers to the peer queue.
     */
    public void add(Collection<Peer> collection) {
        List<Peer> peers = (List<Peer>) collection;
        if (CollectionUtils.isEmpty(peers)) {
            return;
        }
        //todo kongyu 2018-4-25 20:03
        /*
        1.Need to empty all the peer nodes and try to connect retries to 0
        2.it is necessary to determine whether the obtained peer node has already existed in the local area,
        and if it exists, it will not be added, otherwise it will be added
         */
        int newConnNum = 0;
        for (; newConnNum < peers.size(); newConnNum++) {
            Peer peer = peers.get(newConnNum);
            if (null == peer || !peer.valid()) {
                LOGGER.info("peer node is null");
                continue;
            }

            peer.setRetries(0);
            //todo kongyu 2018-04-26 17:03 check cache 是否已经存在被删除的peer节点
            if (peerCache.isCached(peer)) {
                continue;
            }

            if (null == getById(peer.getId())) {
                add(peer);
            }
        }
    }

    /**
     * add peer node to the peers queue
     */
    public void add(Peer peer) {
        peerMap.put(peer.getId(), peer);
    }

    public int count() {
        return peerMap.size();
    }

    /**
     * Get the seed peers after starting for each time.
     */
    public void getSeedPeers() {
        List<Peer> peers = null;
        try {
            peers = registryApi.peers().execute().body();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("get peers: {}", peers);
        add(peers);
    }

    /**
     * Get peer instance by peer id.
     */
    public Peer getById(String id) {
        if (StringUtils.isNotEmpty(id)) {
            return peerMap.get(id);
        }
        return null;
    }

    /**
     * Get peer list by peer ids.
     */
    public List<Peer> getByIds(String[] ids) {
        List<Peer> list = Lists.newArrayList();
        if (null != ids) {
            for (String id : ids) {
                Peer peer = getById(id);
                if (peer != null) {
                    list.add(peer);
                }
            }
        }
        return list;
    }

    /**
     * Return all peers from database.
     */
    public Collection<Peer> getPeers() {
        return peerMap.values();
    }

    /**
     * Shuffle limit peers from database.
     */
    public List<Peer> shuffle(int limit) {
        String rnd = RandomStringUtils.randomAlphanumeric(10);
        return getPeers().stream()
                .filter(peer -> null != peer)
                .sorted((o1, o2) -> {
                    String hash1 = Hashing.sha256().hashString(o1.getId() + rnd, Charsets.UTF_8).toString();
                    String hash2 = Hashing.sha256().hashString(o2.getId() + rnd, Charsets.UTF_8).toString();
                    return hash1.compareTo(hash2);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get local peer instance.
     */
    public Peer getSelf() {
        if (null == self) {
            Peer peer = new Peer();
            peer.setIp(config.getIp());
            // todo baizhengwen 通过upnp设置端口
            peer.setSocketServerPort(config.getSocketPort());
            peer.setHttpServerPort(config.getHttpPort());
            peer.setPubKey(config.getPubKey());
            peer.signature(config.getPriKey());

            self = peer;
            if (!self.valid()) {
                throw new IllegalArgumentException("self peer params invalid");
            }
            add(self);
        }
        return self;
    }

    /**
     * Set value for the local peer and save to database.
     */
    public void setSelf(Peer self) {
        if (null != self) {
            this.self = self;
            add(self);
        }
    }

    /**
     * Delete peer node request
     *
     * @param peer
     */
    public void removePeer(Peer peer) {
        if (null == peer) {
            return;
        }

        Peer self = getSelf();
        //The node cannot delete itself
        if (StringUtils.equals(self.getId(), peer.getId())) {
            return;
        }
        peerMap.remove(peer.getId());
    }

    /**
     * Update peer node request
     *
     * @param peer
     */
    public void updatePeer(Peer peer) {
        if (null != peer) {
            peerMap.put(peer.getId(), peer);
        }
    }

    /**
     * Need to reset the attempt to connect retries to 0
     *
     * @param peer
     */
    public void clearPeerRetries(Peer peer) {
        if (null != peer) {
            peer.setRetries(0);
            peerMap.put(peer.getId(), peer);
        }
    }
}


