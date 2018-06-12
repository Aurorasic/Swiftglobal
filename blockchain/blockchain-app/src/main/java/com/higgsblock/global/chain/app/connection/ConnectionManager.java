package com.higgsblock.global.chain.app.connection;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import com.higgsblock.global.chain.network.socket.Client;
import com.higgsblock.global.chain.network.socket.Server;
import com.higgsblock.global.chain.network.socket.connection.Connection;
import com.higgsblock.global.chain.network.socket.connection.ConnectionLevelEnum;
import com.higgsblock.global.chain.network.socket.connection.NodeRoleEnum;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenjiawei
 * @date 2018-05-26
 */
@Slf4j
@Component
public class ConnectionManager implements InitializingBean {
    /**
     * Maximum number of l3-level connections a node can create as a client. The word "l3-level" means that
     * connection is not miner to witness, nor is witness to another witness.
     */
    private static final int CLIENT_L3_CONN_LIMIT = 5;

    /**
     * Maximum number of l2-level connections a node can create as a client. The word "l2-level" means that
     * connection is miner to witness, or witness to miner. The value should be maximum of witness list size
     * and miner list size.
     */
    private static final int CLIENT_L2_CONN_LIMIT = 11;

    /**
     * Maximum number of l1-level connections a node can create as a client. The word "l1-level" means that
     * connection is witness to another witness. The value should be one less than witness list size.
     */
    private static final int CLIENT_L1_CONN_LIMIT = 10;

    /**
     * Maximum number of l3-level connections a node can accept as a server. The word "l3-level" means that
     * connection is not miner to witness, nor is witness to another witness.
     */
    private static final int SERVER_L3_CONN_LIMIT = 6;

    /**
     * Maximum number of l2-level connections a node can accept as a server. The word "l2-level" means that
     * connection is miner to witness, or witness to miner. The value should be maximum of witness list size
     * and miner list size.
     */
    private static final int SERVER_L2_CONN_LIMIT = 11;

    /**
     * Maximum number of l1-level connections a node can accept as a server. The word "l1-level" means that
     * connection is witness to another witness. The value should be one less than witness list size.
     */
    private static final int SERVER_L1_CONN_LIMIT = 10;

    /**
     * Number of miners in a turn.
     */
    private static final int MINER_PER_TURN = 5;

    /**
     * Maximum number of turns in which a node can accept connections from miners.
     */
    private static final int MINER_TURN_LIMIT = 2;

    /**
     * Extra number of connections allowed by server.
     */
    private static final int SERVER_EXTRA_NUM = 4;

    /**
     * Maximum number of connections a node can accept as a server.
     */
    private static final int SERVER_CONN_LIMIT = SERVER_L1_CONN_LIMIT + MINER_PER_TURN * MINER_TURN_LIMIT + SERVER_L3_CONN_LIMIT + SERVER_EXTRA_NUM;

    /**
     * Minimum timeout in second this node start connections synchronously.
     */
    private static final int SYNC_CONN_TIMEOUT_SECOND = 10;

    /**
     * Connection pool. In this pool, channel id is used as key.
     */
    private Map<String, Connection> connectionMap = Maps.newConcurrentMap();

    /**
     * Temporary connection pool, used to cache connections that do not receive peer information for a moment.
     * In this pool, channel id is used as key.
     */
    private Map<String, Connection> peerUnknownConnectionMap = Maps.newConcurrentMap();

    @Autowired
    private Client client;

    @Autowired
    private Server server;

    @Autowired
    private PeerManager peerManager;

    @Override
    public void afterPropertiesSet() {
        client.setHandler(new ClientHandler(peerManager, this));
        server.setHandler(new ServerHandler(this));
    }


    /**
     * Connect to remote node.
     *
     * @param peer remote node
     */
    private void connect(Peer peer) {
        if (!canConnect(peer)) {
            return;
        }
        client.connect(peer);
    }

    /**
     * Check if this node is allowed to connect to remote node or not.
     *
     * @param peer remote node
     * @return true if this node is allowed to connect to remote node, false otherwise
     */
    private boolean canConnect(Peer peer) {
        if (peer == null) {
            LOGGER.info("Peer to connect to is null");
            return false;
        }
        if (!peerManager.existsInPools(peer)) {
            LOGGER.info("Peer {}, address {} is not in peer pools", peer.getId(), peer.getSocketAddress());
            return false;
        }

        if (peerManager.isSelf(peer)) {
            return false;
        }
        if (contains(peer)) {
            return false;
        }
        if (!levelAllowedAsClient(peer)) {
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
    private boolean contains(Peer peer) {
        return connectionMap.values().stream().anyMatch(connection -> connection.getPeerId().equals(peer.getId()));
    }

    /**
     * Check if this node is allowed to connect to remote node or not, from connection level perspectives.
     *
     * @param peer peer to check
     * @return true if this node is allowed to connect to remote node from connection level perspectives, false otherwise
     */
    private boolean levelAllowedAsClient(Peer peer) {
        ConnectionLevelEnum connectionLevel = calculateConnectionLevel(
                peerManager.getNodeRole(peerManager.getSelf()), peerManager.getNodeRole(peer));

        if (connectionLevel == ConnectionLevelEnum.L1) {
            return countConnections(true, ConnectionLevelEnum.L1) < CLIENT_L1_CONN_LIMIT;
        }
        if (connectionLevel == ConnectionLevelEnum.L2) {
            return countConnections(true, ConnectionLevelEnum.L2) < CLIENT_L2_CONN_LIMIT;
        }
        if (connectionLevel == ConnectionLevelEnum.L3) {
            return countConnections(true, ConnectionLevelEnum.L3) < CLIENT_L3_CONN_LIMIT;
        }

        return false;
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
     * @param isClient        this node is client or not
     * @param connectionLevel connection level
     * @return size of connections meeting the condition
     */
    private int countConnections(boolean isClient, ConnectionLevelEnum connectionLevel) {
        return (int) connectionMap.values().stream().filter(connection ->
                connection.isClient() == isClient && connection.getConnectionLevel() == connectionLevel).count();
    }

    /**
     * Create a connection.
     *
     * @param channel  channel attached to connection
     * @param peer     remote node to connect to
     * @param isClient this node is client or not
     * @return new created connection
     */
    public Connection createConnection(NioSocketChannel channel, Peer peer, boolean isClient) {
        if (contains(peer)) {
            // Null indicates that new connection has not been created because an old one exists.
            return null;
        }

        return connectionMap.computeIfAbsent(
                channel.id().toString(), connectionId -> {
                    Connection connection = new Connection(channel, peer, isClient);

                    ConnectionLevelEnum connectionLevel = calculateConnectionLevel(
                            peerManager.getNodeRole(peerManager.getSelf()), peerManager.getNodeRole(peer));
                    connection.setConnectionLevel(connectionLevel);

                    LOGGER.info("Created a connection, channelId={}, peerId={}, address={}, isClient={}, level={}",
                            connectionId, peer.getId(), peer.getSocketAddress(), isClient, connectionLevel);
                    return connection;
                });
    }


    /**
     * Remove connection.
     *
     * @param connection connection to remove
     */
    public void remove(Connection connection) {
        if (connection == null) {
            return;
        }

        if (connection.close()) {
            String connectionId = connection.getId();
            if (inPeerUnknownConnectionMap(connection)) {
                peerUnknownConnectionMap.remove(connectionId);
            } else {
                connectionMap.remove(connectionId);
            }
            LOGGER.info("Connection {} has been removed", connectionId);
        }
    }


    /**
     * Start this node as server and wait to accept new connections.
     */
    public void startServer() {
        server.start();
    }

    /**
     * Check if number of connections received reached the limitation.
     *
     * @return true if number of connections received does not reached the limitation, false otherwise
     */
    public boolean connectionNumberAllowedAsServer() {
        return countConnectionsAsServer() < SERVER_CONN_LIMIT;
    }

    /**
     * Get number of connections received as server.
     *
     * @return Number of connections received as server
     */
    private int countConnectionsAsServer() {
        return (int) (connectionMap.values().stream().filter(connection -> !connection.isClient()).count()
                + peerUnknownConnectionMap.values().stream().filter(connection -> !connection.isClient()).count());
    }

    /**
     * Create a connection.
     *
     * @param channel  channel attached to connection
     * @param isClient this node is client or not
     * @return new created connection
     */
    public Connection createConnection(NioSocketChannel channel, boolean isClient) {
        return peerUnknownConnectionMap.computeIfAbsent(channel.id().toString(), connectionId -> {
            Connection connection = new Connection(channel, isClient);

            LOGGER.info("Created a connection to channelId={}, isClient={}", connectionId, isClient);
            return connection;
        });
    }

    /**
     * Process connection that received peer information.
     *
     * @param connection connection to process
     * @param peer       peer information from client
     */
    public void receivePeer(Connection connection, Peer peer) {
        if (!inPeerUnknownConnectionMap(connection)) {
            return;
        }

        // Remove connection from pool if peer information from client is invalid.
        if (peer == null || !peer.valid()) {
            LOGGER.warn("Hello peer is invalid");
            remove(connection);
            return;
        }

        // Peer shall be appended to the peer pool if not in it.
        if (!peerManager.contains(peer)) {
            peerManager.addOrUpdate(peer);
        }
        peerManager.clearPeerRetries(peer);

        if (peerManager.isSelf(peer)) {
            LOGGER.info("Peer {}, address {} is this node itself", peer.getId(), peer.getSocketAddress());
            remove(connection);
            return;
        }

        // The remote node is not allowed to send the same peer two times in one connection. If not so,
        // connection will be removed.
        // If in two connections, the first one will be kept, the other will be removed.
        if (contains(peer)) {
            LOGGER.info("Peer {}, address {} is in connection pools", peer.getId(), peer.getSocketAddress());
            remove(connection);
            return;
        }

        ConnectionLevelEnum connectionLevel = ConnectionManager.calculateConnectionLevel(
                peerManager.getNodeRole(peerManager.getSelf()), peerManager.getNodeRole(peer));

        if (levelAllowedAsServer(connectionLevel)) {
            connection.setPeer(peer);
            connection.setConnectionLevel(connectionLevel);
            moveToConnectionMap(connection);
        } else {
            remove(connection);
        }
    }

    /**
     * Check if connection is in temporary pool.
     *
     * @param connection connection to check
     * @return true if connection is in temporary pool, false otherwise
     */
    private boolean inPeerUnknownConnectionMap(Connection connection) {
        return peerUnknownConnectionMap.containsKey(connection.getId());
    }

    /**
     * Check if this node can accepts remote node or not, from connection level perspectives.
     *
     * @param connectionLevel level of connection from client to this node
     * @return true if this node can accepts remote node from connection level perspectives, false otherwise
     */
    private boolean levelAllowedAsServer(ConnectionLevelEnum connectionLevel) {
        if (connectionLevel == ConnectionLevelEnum.L1) {
            return countConnections(false, ConnectionLevelEnum.L1) < SERVER_L1_CONN_LIMIT;
        }
        if (connectionLevel == ConnectionLevelEnum.L2) {
            return countConnections(false, ConnectionLevelEnum.L2) < SERVER_L2_CONN_LIMIT;
        }
        if (connectionLevel == ConnectionLevelEnum.L3) {
            return countConnections(false, ConnectionLevelEnum.L3) < SERVER_L3_CONN_LIMIT;
        }
        return false;

    }

    /**
     * Move connection from temporary pool to pool in which every connection has peer information.
     *
     * @param connection conncetion to move
     */
    private void moveToConnectionMap(Connection connection) {
        connectionMap.putIfAbsent(connection.getId(), connection);
        peerUnknownConnectionMap.remove(connection.getId());
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

    /**
     * Remove connection by peer id.
     *
     * @param peerId peer id of connection to remove
     */
    public void remove(String peerId) {
        Connection connection = getConnectionByPeerId(peerId);
        remove(connection);
    }

    /**
     * Get connection by peer id.
     *
     * @param peerId peer id of wanted connection
     * @return wanted connection
     */
    public Connection getConnectionByPeerId(String peerId) {
        return getActivatedConnections().stream().filter(
                connection -> connection.getPeerId().equals(peerId)).findFirst().orElse(null);
    }

    /**
     * Get peers of active connections.
     */
    public List<Peer> getActivatedPeers() {
        return getActivatedConnections().stream().map(Connection::getPeer).collect(Collectors.toList());
    }

    /**
     * Get connections via which this node can send or receive message.
     */
    public List<Connection> getActivatedConnections() {
        return connectionMap.values().stream().filter(Connection::isActivated).collect(Collectors.toList());
    }


    /**
     * Remove every connection which has not received peer information within timeout.
     */
    public void removePeerUnknownConnections() {
        peerUnknownConnectionMap.values().forEach(connection -> {
            if (connection.waitPeerTimeout()) {
                remove(connection);
                LOGGER.info("Connection {} is removed for the reason not receiving peer information within timeout", connection.getId());
            }
        });
    }

    /**
     * Refresh level of connections in pool.
     */
    public void refreshConnectionLevel() {
        connectionMap.values().forEach(connection -> {
            ConnectionLevelEnum newConnectionLevel = calculateConnectionLevel(
                    peerManager.getNodeRole(peerManager.getSelf()), peerManager.getNodeRole(connection.getPeer()));

            ConnectionLevelEnum oldConnectionLevel = connection.getConnectionLevel();
            if (oldConnectionLevel != newConnectionLevel) {
                connection.setConnectionLevel(newConnectionLevel);
                LOGGER.info("Level of connection {} changes from {} to {}", connection.getId(), oldConnectionLevel, newConnectionLevel);
            }
        });
    }

    /**
     * Remove extra connections.
     */
    public void removeExtraConnections() {
        removeExtraConnections(true, ConnectionLevelEnum.L1, CLIENT_L1_CONN_LIMIT);
        removeExtraConnections(true, ConnectionLevelEnum.L2, CLIENT_L2_CONN_LIMIT);
        removeExtraConnections(true, ConnectionLevelEnum.L3, CLIENT_L3_CONN_LIMIT);

        removeExtraConnections(false, ConnectionLevelEnum.L1, SERVER_L1_CONN_LIMIT);
        removeExtraConnections(false, ConnectionLevelEnum.L2, SERVER_L2_CONN_LIMIT);
        removeExtraConnections(false, ConnectionLevelEnum.L3, SERVER_L3_CONN_LIMIT);
    }

    /**
     * Remove extra connections.
     *
     * @param isClient        this node is client or not
     * @param connectionLevel level of connection
     * @param numberAllowed   number of connections allowed for the level
     */
    private void removeExtraConnections(boolean isClient, ConnectionLevelEnum connectionLevel, int numberAllowed) {
        List<Connection> connections = connectionMap.values().stream().filter(connection ->
                connection.isClient() == isClient && connection.getConnectionLevel() == connectionLevel).collect(Collectors.toList());

        for (int extraIndex = numberAllowed, length = connections.size(); extraIndex < length; extraIndex++) {
            remove(connections.get(extraIndex));
        }
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
            witnesses.stream().filter(witness -> !witness.getId().equals(self.getId())).forEach(this::connect);
        }
    }

    /**
     * Remove specified size of l3-level connections randomly.
     *
     * @param numberToRemove number of connections to remove
     */
    public void removeL3RandomConnections(int numberToRemove) {
        for (int i = 0; i < numberToRemove; i++) {
            Connection selectedConnection = connectionMap.values().stream().filter(connection ->
                    connection.getConnectionLevel() == ConnectionLevelEnum.L3
                            && System.currentTimeMillis() - connection.getCreatedTime()
                            > (8 + Math.random() * 4) * TimeUnit.HOURS.toMillis(1)).findFirst().orElse(null);

            if (selectedConnection != null) {
                remove(selectedConnection);
            }
        }
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

        int validNumber = 0;
        for (Peer peer : peers) {
            if (peer == null) {
                LOGGER.info("Peer is null");
                continue;
            }
            if (!peer.valid()) {
                LOGGER.info("Peer params is invalid, peerId = {}, address = {}", peer.getId(), peer.getSocketAddress());
                peerManager.removePeer(peer);
                continue;
            }

            validNumber++;
            connect(peer);
        }
        LOGGER.info("Try to create {} new connections", validNumber);
    }


    /**
     * Connect to peers synchronously.
     *
     * @param peers           peers to connect to
     * @param needActiveSize  number of wanted active connections
     * @param timeoutInSecond timeout to get active connections
     * @return true if success, false otherwise
     */
    private boolean syncConnect(Collection<Peer> peers, int needActiveSize, int timeoutInSecond) {
        long timeout = TimeUnit.SECONDS.toMillis(timeoutInSecond);
        long startTime = System.currentTimeMillis();

        for (Peer peer : peers) {
            connect(peer);
        }

        do {
            if (getActivatedConnections().size() >= needActiveSize) {
                return true;
            }
        } while (System.currentTimeMillis() - startTime < timeout);

        return false;
    }

    /**
     * Connect to peers from local and registry center synchronously.
     *
     * @param leastActiveSize if number of active connections is less than leastActiveSize, the method itself will be
     *                        invoked to block application to go on
     * @param needActiveSize  number of wanted active connections
     * @param timeoutInSecond timeout to get active connections
     * @return true if success, false otherwise
     */
    public boolean connectToPeers(int leastActiveSize, int needActiveSize, int timeoutInSecond) {
        if (timeoutInSecond < SYNC_CONN_TIMEOUT_SECOND) {
            throw new IllegalArgumentException("Time to wait connection must be greater than " + SYNC_CONN_TIMEOUT_SECOND + " seconds");
        }

        List<Peer> peers = Lists.newArrayList(peerManager.getPeers());
        peers.addAll(peerManager.getWitnessPeers());
        boolean done = syncConnect(peers, needActiveSize, timeoutInSecond - SYNC_CONN_TIMEOUT_SECOND);
        if (!done) {
            peerManager.getSeedPeers();
            peers = Lists.newArrayList(peerManager.getPeers());
            peers.addAll(peerManager.getWitnessPeers());
            done = syncConnect(peers, needActiveSize, SYNC_CONN_TIMEOUT_SECOND);
        }

        Collection<Connection> activeConnections = getActivatedConnections();
        LOGGER.info("Number of activated connections: " + activeConnections.size());
        activeConnections.forEach(connection ->
                LOGGER.info("Connection has been activated. Peer {}, channel id {}, remote address {}",
                        connection.getPeerId(), connection.getId(), connection.getPeer().getSocketAddress()));

        if (done) {
            return true;
        }

        if (activeConnections.size() < leastActiveSize) {
            return connectToPeers(leastActiveSize, needActiveSize, timeoutInSecond);
        } else {
            return false;
        }
    }
}
