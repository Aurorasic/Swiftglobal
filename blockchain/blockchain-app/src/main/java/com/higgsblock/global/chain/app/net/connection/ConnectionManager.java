package com.higgsblock.global.chain.app.net.connection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.higgsblock.global.chain.app.net.constants.ConnectionLevelEnum;
import com.higgsblock.global.chain.app.net.constants.NodeRoleEnum;
import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import com.higgsblock.global.chain.network.socket.Client;
import com.higgsblock.global.chain.network.socket.Server;
import com.higgsblock.global.chain.network.socket.constants.ChannelType;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Connection manager among peers.
 *
 * @author chenjiawei
 * @date 2018-05-26
 */
@Slf4j
@Component
public class ConnectionManager {
    /**
     * Maximum number of l3-level connections a node can create as a client. The word "l3-level" means that
     * connection is not miner to witness, nor is witness to another witness.
     */
    private static final int L3_CONN_OUT_LIMIT = 2;

    /**
     * Maximum number of l2-level connections a node can create as a client. The word "l2-level" means that
     * connection is miner to witness, or witness to miner. The value should be maximum of witness list size
     * and miner list size.
     */
    private static final int L2_CONN_OUT_LIMIT = 11;

    /**
     * Maximum number of l1-level connections a node can create as a client. The word "l1-level" means that
     * connection is witness to another witness. The value should be one less than witness list size.
     */
    private static final int L1_CONN_OUT_LIMIT = 10;

    /**
     * Maximum number of l3-level connections a node can accept as a server. The word "l3-level" means that
     * connection is not miner to witness, nor is witness to another witness.
     */
    private static final int L3_CONN_IN_LIMIT = 30;

    /**
     * Maximum number of l2-level connections a node can accept as a server. The word "l2-level" means that
     * connection is miner to witness, or witness to miner. The value should be maximum of witness list size
     * and miner list size.
     */
    private static final int L2_CONN_IN_LIMIT = 11;

    /**
     * Maximum number of l1-level connections a node can accept as a server. The word "l1-level" means that
     * connection is witness to another witness. The value should be one less than witness list size.
     */
    private static final int L1_CONN_IN_LIMIT = 10;

    /**
     * Connection pool. In this pool, channel id is used as key.
     */
    private Map<String, Connection> connectionMap = Maps.newConcurrentMap();

    @Autowired
    private Client client;
    @Autowired
    private Server server;
    @Autowired
    private PeerManager peerManager;

    /**
     * Connect to remote node.
     *
     * @param peer remote node
     */
    private void connect(Peer peer) {
        if (!canConnect(peer)) {
            return;
        }
        peerManager.removePeer(peer);
        client.connect(peer.getIp(), peer.getSocketServerPort());
    }

    /**
     * Check if this node is allowed to connect to remote node or not.
     *
     * @param peer remote node
     * @return true if this node is allowed to connect to remote node, false otherwise
     */
    private boolean canConnect(Peer peer) {
        if (peer == null) {
            return false;
        }

        if (peerManager.isSelf(peer)) {
            return false;
        }

        if (isConnected(peer)) {
            return false;
        }

        ConnectionLevelEnum level = calculateConnectionLevel(peerManager.getNodeRole(peerManager.getSelf()), peerManager.getNodeRole(peer));
        if (!isAllowedConnectOut(level)) {
            return false;
        }
        return true;
    }

    /**
     * Check if connection pool contains connection of peer or not.
     *
     * @param peer peer to check
     * @return true if connection pool contains connection of peer, false otherwise
     */
    private boolean isConnected(Peer peer) {
        return connectionMap.values().stream().anyMatch(connection -> StringUtils.equals(connection.getPeerId(), peer.getId()));
    }

    /**
     * Calculate level of a connection.
     *
     * @param selfNodeRole   role this node plays
     * @param remoteNodeRole role remote node plays
     * @return connection level
     */
    private static ConnectionLevelEnum calculateConnectionLevel(NodeRoleEnum selfNodeRole, NodeRoleEnum remoteNodeRole) {
        int weight = selfNodeRole.getWeight() + remoteNodeRole.getWeight();
        switch (weight) {
            case 6:
                // Both are witnesses
                return ConnectionLevelEnum.L1;
            case 5:
                // One is a witness and the other is a miner
                return ConnectionLevelEnum.L2;
            default:
                return ConnectionLevelEnum.L3;
        }
    }

    /**
     * Count connection size with specified this node being client or not and connection level.
     *
     * @param type            this node is client or not
     * @param connectionLevel connection level
     * @return size of connections meeting the condition
     */
    private int countConnections(ChannelType type, ConnectionLevelEnum connectionLevel) {
        return (int) connectionMap.values().stream()
                .filter(connection -> connection.getType() == type
                        && connection.getConnectionLevel() == connectionLevel)
                .count();
    }


    /**
     * Remove connection.
     *
     * @param connection connection to remove
     */
    public void close(Connection connection) {
        if (connection == null) {
            return;
        }
        String channelId = connection.getChannelId();
        closeByChannelId(channelId);
    }

    public void closeByChannelId(String channelId) {
        if (null == channelId) {
            return;
        }
        Connection connection = connectionMap.remove(channelId);
        if (null != connection) {
            connection.close();
        }
        LOGGER.info("Connection has been removed, channelId={}", channelId);
    }

    /**
     * Remove connection by peer id.
     *
     * @param peerId peer id of connection to remove
     */
    public void closeByPeerId(String peerId) {
        Connection connection = getConnectionByPeerId(peerId);
        close(connection);
    }


    /**
     * Start this node as server and wait to accept new connections.
     */
    public void startServer() {
        server.start();
    }

    /**
     * Create a connection.
     *
     * @param channel channel attached to connection
     * @param type    this node is client or not
     * @return new created connection
     */
    public Connection createConnection(Channel channel, ChannelType type) {
        if (null == channel) {
            return null;
        }
        return connectionMap.computeIfAbsent(channel.id().toString(), connectionId -> {
            Connection connection = Connection.newInstance(channel, type);

            LOGGER.info("Created a connection, channelId={}, type={}", connectionId, type);
            return connection;
        });
    }

    public void active(String channelId, Peer peer) {
        if (null == channelId) {
            return;
        }
        Connection connection = connectionMap.get(channelId);
        if (null == connection) {
            return;
        }

        // Peer shall be appended to the peer pool if not in it.
        if (!peerManager.contains(peer)) {
            peerManager.add(peer);
        }

        if (peerManager.isSelf(peer)) {
            close(connection);
            return;
        }

        // The remote node is not allowed to send the same peer two times in one connection. If not so,
        // connection will be removed.
        // If in two connections, the first one will be kept, the other will be removed.
        if (isConnected(peer)) {
            close(connection);
            return;
        }

        ConnectionLevelEnum level = ConnectionManager.calculateConnectionLevel(
                peerManager.getNodeRole(peerManager.getSelf()), peerManager.getNodeRole(peer));

        if (isAllowedConnect(level, connection.getType())) {
            connection.activate(peer);
            connection.setConnectionLevel(level);
        } else {
            close(connection);
        }
    }

    private boolean isAllowedConnect(ConnectionLevelEnum level, ChannelType type) {
        if (ChannelType.IN == type) {
            return isAllowedConnectIn(level);
        }
        return isAllowedConnectOut(level);
    }

    private boolean isAllowedConnectOut(ConnectionLevelEnum level) {
        if (level == ConnectionLevelEnum.L1) {
            return countConnections(ChannelType.OUT, ConnectionLevelEnum.L1) < L1_CONN_OUT_LIMIT;
        }
        if (level == ConnectionLevelEnum.L2) {
            return countConnections(ChannelType.OUT, ConnectionLevelEnum.L2) < L2_CONN_OUT_LIMIT;
        }
        if (level == ConnectionLevelEnum.L3) {
            return countConnections(ChannelType.OUT, ConnectionLevelEnum.L3) < L3_CONN_OUT_LIMIT;
        }
        return false;
    }

    private boolean isAllowedConnectIn(ConnectionLevelEnum level) {
        if (level == ConnectionLevelEnum.L1) {
            return countConnections(ChannelType.IN, ConnectionLevelEnum.L1) < L1_CONN_IN_LIMIT;
        }
        if (level == ConnectionLevelEnum.L2) {
            return countConnections(ChannelType.IN, ConnectionLevelEnum.L2) < L2_CONN_IN_LIMIT;
        }
        if (level == ConnectionLevelEnum.L3) {
            return countConnections(ChannelType.IN, ConnectionLevelEnum.L3) < L3_CONN_IN_LIMIT;
        }
        return false;
    }

    /**
     * Get witness connections.
     *
     * @return connections in which remote nodes are witnesses.
     */
    public Collection<Connection> getWitnessConnections() {
        Collection<Connection> connections = Lists.newArrayList();

        for (Peer witness : peerManager.getWitnessPeers()) {
            Connection connection = getConnectionByPeer(witness);
            if (connection != null) {
                connections.add(connection);
            }
        }

        return connections;
    }

    /**
     * Get connection by peer.
     */
    private Connection getConnectionByPeer(Peer peer) {
        if (peer == null) {
            return null;
        }
        return getConnectionByPeerId(peer.getId());
    }

    public Connection getConnectionByChannelId(String channelId) {
        if (null == channelId) {
            return null;
        }
        return connectionMap.get(channelId);
    }

    /**
     * Get connection by peer id.
     *
     * @param peerId peer id of wanted connection
     * @return wanted connection
     */
    public Connection getConnectionByPeerId(String peerId) {
        return connectionMap.values().stream()
                .filter(Connection::isActivated)
                .filter(connection -> StringUtils.equals(peerId, connection.getPeerId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get peers of active connections.
     */
    public List<Peer> getActivatedPeers() {
        return getActivatedConnections().stream()
                .filter(Connection::isActivated)
                .map(Connection::getPeer)
                .collect(Collectors.toList());
    }

    public List<Connection> getAllConnections() {
        return Lists.newLinkedList(connectionMap.values());
    }

    /**
     * Get connections via which this node can send or receive message.
     */
    public List<Connection> getActivatedConnections() {
        return connectionMap.values().stream()
                .filter(Connection::isActivated)
                .collect(Collectors.toList());
    }


    /**
     * Remove every connection which has not received peer information within timeout.
     */
    public void removeInactivatedConnections() {
        connectionMap.values().stream()
                .filter(connection -> !connection.isActivated() && connection.isHandshakeTimeOut())
                .forEach(connection -> {
                    String channelId = connection.getChannelId();
                    close(connection);
                    LOGGER.info("Connection {} is removed for the reason not receiving peer information within timeout", channelId);
                });
    }

    /**
     * Refresh level of connections in pool.
     */
    public void refreshConnectionLevel() {
        connectionMap.values().stream()
                .filter(Connection::isActivated)
                .forEach(connection -> {
                    ConnectionLevelEnum newConnectionLevel = calculateConnectionLevel(
                            peerManager.getNodeRole(peerManager.getSelf()),
                            peerManager.getNodeRole(connection.getPeer()));

                    ConnectionLevelEnum oldConnectionLevel = connection.getConnectionLevel();
                    if (oldConnectionLevel != newConnectionLevel) {
                        connection.setConnectionLevel(newConnectionLevel);
                        LOGGER.debug("Level of connection {} changes from {} to {}", connection.getChannelId(), oldConnectionLevel, newConnectionLevel);
                    }
                });
    }

    /**
     * Remove extra connections.
     */
    public void removeExtraConnections() {
        removeExtraConnections(ChannelType.OUT, ConnectionLevelEnum.L1, L1_CONN_OUT_LIMIT);
        removeExtraConnections(ChannelType.OUT, ConnectionLevelEnum.L2, L2_CONN_OUT_LIMIT);
        removeExtraConnections(ChannelType.OUT, ConnectionLevelEnum.L3, L3_CONN_OUT_LIMIT);

        removeExtraConnections(ChannelType.IN, ConnectionLevelEnum.L1, L1_CONN_IN_LIMIT);
        removeExtraConnections(ChannelType.IN, ConnectionLevelEnum.L2, L2_CONN_IN_LIMIT);
        removeExtraConnections(ChannelType.IN, ConnectionLevelEnum.L3, L3_CONN_IN_LIMIT);
    }

    /**
     * Remove extra connections.
     *
     * @param type            this node is client or not
     * @param connectionLevel level of connection
     * @param numberAllowed   number of connections allowed for the level
     */
    private void removeExtraConnections(ChannelType type, ConnectionLevelEnum connectionLevel, int numberAllowed) {
        connectionMap.values().stream()
                .filter(connection -> connection.getType() == type
                        && connection.getConnectionLevel() == connectionLevel)
                .skip(numberAllowed)
                .forEach(this::close);
    }

    /**
     * Create special connections.
     */
    public void createSpecialConnections() {
        Peer self = peerManager.getSelf();
        NodeRoleEnum selfNodeRole = peerManager.getNodeRole(self);
        List<Peer> witnesses = peerManager.getWitnessPeers();

        // Connect to witnesses if this node is miner.
        if (selfNodeRole == NodeRoleEnum.MINER) {
            witnesses.forEach(this::connect);
        }

        // Connect to other witnesses if this node is witness.
        if (selfNodeRole == NodeRoleEnum.WITNESS) {
            witnesses.stream().filter(witness -> !StringUtils.equals(witness.getId(), self.getId())).forEach(this::connect);
        }
    }

    /**
     * Remove specified size of l3-level connections randomly.
     *
     * @param numberToRemove number of connections to remove
     */
    public void removeL3RandomConnections(int numberToRemove) {
        connectionMap.values().stream()
                .filter(connection -> connection.getConnectionLevel() == ConnectionLevelEnum.L3
                        && connection.getAge() > TimeUnit.MINUTES.toMillis(10 + RandomUtils.nextInt(50)))
                .limit(numberToRemove)
                .forEach(this::close);
    }

    /**
     * Get peers randomly and connect to them.
     */
    public void createRandomConnections() {
        List<Peer> peers = peerManager.shuffle(10);
        if (peers.isEmpty()) {
            LOGGER.warn("No peers to connect");
            return;
        }

        for (Peer peer : peers) {
            if (peer == null) {
                continue;
            }
            if (!peer.valid()) {
                peerManager.removePeer(peer);
                continue;
            }
            connect(peer);
        }
    }

}
