package com.higgsblock.global.chain.network;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.network.api.IRegistryApi;
import com.higgsblock.global.chain.network.config.PeerConfig;
import com.higgsblock.global.chain.network.config.PeerConstant;
import com.higgsblock.global.chain.network.config.RegistryConfig;
import com.higgsblock.global.chain.network.socket.PeerCache;
import com.higgsblock.global.chain.network.socket.connection.NodeRoleEnum;
import com.higgsblock.global.chain.network.upnp.UpnpManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The peer manager provides basic methods to add peers into database or to find peers from database.
 *
 * @author chenjiawei
 * @date 2018 -05-22
 */
@Component
@Slf4j
public class PeerManager {
    /**
     * The Config.
     */
    @Autowired
    private PeerConfig config;

    /**
     * The Peer cache.
     */
    @Autowired
    private PeerCache peerCache;

    /**
     * The Registry api.
     */
    @Autowired
    private IRegistryApi registryApi;

    /**
     * The Self.
     */
    private Peer self;

    /**
     * The Peer map.
     */
    private Map<String, Peer> peerMap = new ConcurrentHashMap<>();

    /**
     * The Registry config.
     */
    @Autowired
    private RegistryConfig registryConfig;

    /**
     * List of witnesses.
     */
    @Getter
    private List<Peer> witnessPeers = Lists.newArrayList();
    /**
     * List of miners.
     */
    @Getter
    @Setter
    private List<String> minerAddresses = Lists.newArrayList();

    /**
     * Sets witness peers.
     *
     * @param witnessPeers the witness peers
     */
    public void setWitnessPeers(List<Peer> witnessPeers) {
        this.witnessPeers.clear();
        for (Peer peer : witnessPeers) {
            // if (peer == null || !peer.valid()) {  TODO  yanghuadong  for test 2018-05-28
            if (peer == null) {
                continue;
            }
            this.witnessPeers.add(peer);
        }
    }

    /**
     * Add peers to the peer queue.
     *
     * @param collection the collection
     */
    public void add(Collection<Peer> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return;
        }

        List<Peer> peers = Lists.newArrayList(collection);

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
            if (peerCache.isCached(peer)) {
                continue;
            }

            if (null == getById(peer.getId())) {
                addOrUpdate(peer);
            }
        }
    }

    /**
     * add peer node to the peers queue
     *
     * @param peer the peer
     */
    public void addOrUpdate(Peer peer) {
        if (null == peer) {
            return;
        }

        peerMap.put(peer.getId(), peer);
    }

    /**
     * Clear peer.
     */
    public void clearPeer() {
        peerMap.clear();
    }

    /**
     * Count int.
     *
     * @return the int
     */
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
     *
     * @param id the id
     * @return the by id
     */
    public Peer getById(String id) {
        if (StringUtils.isNotEmpty(id)) {
            return peerMap.get(id);
        }
        return null;
    }

    /**
     * Return all peers from database.
     *
     * @return the peers
     */
    public Collection<Peer> getPeers() {
        return peerMap.values();
    }

    /**
     * Shuffle limit peers from database.
     *
     * @param limit the limit
     * @return the list
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
     * Load self peer info boolean.
     *
     * @return the boolean
     */
    public boolean loadSelfPeerInfo() {
        Peer peer = new Peer();
        peer.setIp(config.getIp());
        peer.setSocketServerPort(config.getSocketPort());
        peer.setHttpServerPort(config.getHttpPort());
        peer.setPubKey(config.getPubKey());
        peer.signature(config.getPriKey());
        if (!peer.valid()) {
            throw new IllegalArgumentException("self peer params invalid");
        }

        setSelf(peer);
        peerCache.setCached(peer);
        return true;
    }

    /**
     * Load neighbor peers boolean.
     *
     * @return the boolean
     */
    public boolean loadNeighborPeers() {
        // load neighbor peers from local, if some peers cannot be connected, fetch new peers from register
        // 1.load neighbor peers from local
        Collection<Peer> localPeers = getPeers();
        if (CollectionUtils.isEmpty(localPeers) || localPeers.size() < PeerConstant.MIN_LOCAL_PEER_COUNT) {
            this.getSeedPeers();
        } else {
            this.add(localPeers);
        }

        return true;
    }

    /**
     * Get local peer instance.
     *
     * @return the self
     */
    public Peer getSelf() {
        if (null == self) {
            loadSelfPeerInfo();
        }

        return self;
    }

    /**
     * Set value for the local peer and save to database.
     *
     * @param self the self
     */
    public void setSelf(Peer self) {
        if (null != self) {
            this.self = self;
            addOrUpdate(self);
        }
    }

    /**
     * Delete peer node request
     *
     * @param peer the peer
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
     * @param peer the peer
     */
    public void updatePeer(Peer peer) {
        if (null != peer) {
            peerMap.put(peer.getId(), peer);
        }
    }

    /**
     * Need to reset the attempt to connect retries to 0
     *
     * @param peer the peer
     */
    public void clearPeerRetries(Peer peer) {
        if (null != peer) {
            peer.setRetries(0);
            peerMap.put(peer.getId(), peer);
        }
    }

    /**
     * Check if pool contains specific peer or not.
     *
     * @param peer peer to check
     * @return true if pool contains the peer, false otherwise
     */
    public boolean contains(Peer peer) {
        return getById(peer.getId()) != null;
    }

    /**
     * Check if specific peer is this node itself or not.
     *
     * @param peer peer to check
     * @return true if the peer is this node itself, false otherwise
     */
    public boolean isSelf(Peer peer) {
        if (peer == null) {
            throw new IllegalArgumentException("Peer is null");
        }
        if (getSelf() == null) {
            throw new IllegalArgumentException("Self peer is null");
        }
        return peer.getId().equals(getSelf().getId());
    }

    /**
     * Check if peer is a witness or not.
     *
     * @param peer peer to check
     * @return true if the peer is a witness, false otherwise
     */
    public boolean isWitness(Peer peer) {
        if (peer == null) {
            throw new IllegalArgumentException("Peer is null");
        }
        return witnessPeers.stream().filter(
                witness -> witness != null).anyMatch(witness -> witness.getId().equals(peer.getId()));
    }

    /**
     * Triggered by failing to connect to the peer.
     */
    public void onTryCompleted(Peer peer) {
        peer.onTryCompleted();
        if (peer.retryExceedLimitation()) {
            removePeer(peer);
            peerCache.setCached(peer);
        } else {
            updatePeer(peer);
        }
    }

    /**
     * Calculate the node role.
     *
     * @param peer the target node to be calculated
     * @return node role the peer plays
     */
    public NodeRoleEnum getNodeRole(Peer peer) {
        return getNodeRole(peer.getId());
    }

    /**
     * Calculate the node role.
     *
     * @param peerId id of the target node to be calculated
     * @return node role the peer plays
     */
    private NodeRoleEnum getNodeRole(String peerId) {
        if (witnessPeers.stream().anyMatch(witness -> witness.getId().equals(peerId))) {
            return NodeRoleEnum.WITNESS;
        }
        if (minerAddresses.stream().anyMatch(miner -> miner.equals(peerId))) {
            return NodeRoleEnum.MINER;
        }
        return NodeRoleEnum.PEER;
    }


    /**
     * Exists in pools boolean.
     *
     * @param peer the peer
     * @return the boolean
     */
    public boolean existsInPools(Peer peer) {
        if (peer == null) {
            return false;
        }
        String peerId = peer.getId();
        return peerMap.containsKey(peerId)
                || minerAddresses.contains(peerId)
                || witnessPeers.stream().filter(witness -> witness != null).anyMatch(witness -> witness.getId().equals(peerId));
    }

    /**
     * Report to registry.
     */
    public void reportToRegistry() {
        try {
            boolean result = this.registryApi.report(getSelf()).execute().body();
            LOGGER.info("report self info to register({}) {}", registryConfig.toString(), result);
        } catch (Exception e) {
            LOGGER.error(String.format("report peer info to register error:%s", e.getMessage()), e);
        }
    }
}