package com.higgsblock.global.chain.app.net.peer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.net.api.IRegistryApi;
import com.higgsblock.global.chain.app.net.constants.NodeRoleEnum;
import com.higgsblock.global.chain.network.config.PeerConfig;
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
     * The constant MIN_LOCAL_PEER_COUNT.
     */
    public static final int MIN_LOCAL_PEER_COUNT = 2;
    /**
     * The Config.
     */
    @Autowired
    private PeerConfig config;

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
    private Cache<String, Peer> peerCache = Caffeine.newBuilder().maximumSize(100).build();

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
            if (peer == null) {
                continue;
            }
            this.witnessPeers.add(peer);
        }
    }

    /**
     * Add peers to the peer queue.
     *
     * @param peers the collection
     */
    public void add(Collection<Peer> peers) {
        if (CollectionUtils.isEmpty(peers)) {
            return;
        }

        peers.forEach(this::add);
    }

    /**
     * add peer node to the peers queue
     *
     * @param peer the peer
     */
    public void add(Peer peer) {
        if (null == peer) {
            return;
        }

        peerCache.put(peer.getId(), peer);
    }

    /**
     * Count int.
     *
     * @return the int
     */
    public long count() {
        peerCache.cleanUp();
        return peerCache.estimatedSize();
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
            return peerCache.getIfPresent(id);
        }
        return null;
    }

    /**
     * Return all peers from database.
     *
     * @return the peers
     */
    public Collection<Peer> getPeers() {
        return peerCache.asMap().values();
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
            add(self);
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
        peerCache.invalidate(peer.getId());
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
     * Calculate the node role.
     *
     * @param peer the target node to be calculated
     * @return node role the peer plays
     */
    public NodeRoleEnum getNodeRole(Peer peer) {
        if (null == peer) {
            return NodeRoleEnum.PEER;
        }
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
     * Report to registry.
     */
    public void reportToRegistry() {
        try {
            this.registryApi.report(getSelf()).execute().body();
        } catch (Exception e) {
            LOGGER.error(String.format("report peer info to register error:%s", e.getMessage()), e);
        }
    }
}