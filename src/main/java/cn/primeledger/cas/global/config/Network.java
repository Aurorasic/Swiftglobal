package cn.primeledger.cas.global.config;

/**
 * Network configuration.
 *
 * @author  zhao xiaogang
 * */
public class Network {
    private int maxOutboundConnections;
    private int maxInboundConnections;
    private int maxInboundConnectionsPerIp;

    private int maxPeersToDiscoverCount;
    private int p2pServerListeningPort;
    private int p2pConnectionTimeout;
    private boolean peerPersistEnabled;
    private NetworkType networkType;
    private int version;


    public Network(Builder builder) {
        maxOutboundConnections = builder.maxOutboundConnections;
        maxInboundConnections = builder.maxInboundConnections;
        maxInboundConnectionsPerIp = builder.maxInboundConnectionsPerIp;

        maxPeersToDiscoverCount = builder.maxPeersToDiscoverCount;
        p2pServerListeningPort = builder.p2pServerListeningPort;
        p2pConnectionTimeout = builder.p2pConnectionTimeout;
        peerPersistEnabled = builder.peerPersistEnabled;
        networkType = builder.networkType;
        version = builder.version;
    }

    /**
     * Return the max number of outbound connections.
     */
    public int maxOutboundConnections() {
        return maxOutboundConnections;
    }

    /**
     * Return the max number of inbound connections.
     */
    public int maxInboundConnections() {
        return maxInboundConnections();
    }

    /**
     * Return the max number of inbound connections of each unique IP.
     */
    public int maxInboundConnectionsPerIp() {
        return maxInboundConnectionsPerIp;
    }

    /**
     * Return the max number of peers to discover.
     */
    public int maxPeersToDiscoverCount() {
        return maxPeersToDiscoverCount;
    }

    /**
     * Return the p2p server's listening port number.
     */
    public int p2pServerListeningPort() {
        return p2pServerListeningPort;
    }

    /**
     * Return the version for p2p network.
     */
    public int version() {
        return version;
    }

    /**
     * Return whether the peer needs to be persisted into the database.
     */
    public boolean peerPersistEnabled() {
        return peerPersistEnabled;
    }

    /**
     * Return the p2p network type.(mainnet, testnet, devnet)
     */
    public NetworkType type() {
        return networkType;
    }

    /**
     * Return the connection timeout for p2p network.
     */
    public int p2pConnectionTimeout() {
        return p2pConnectionTimeout;
    }


    public static class Builder {
        int maxOutboundConnections = 20;
        int maxInboundConnections = 20;
        int maxInboundConnectionsPerIp = 5;
        int maxPeersToDiscoverCount = 100;

        int p2pServerListeningPort = 8000;
        int p2pConnectionTimeout = 5000;

        boolean peerPersistEnabled;
        int version = 0;

        NetworkType networkType = NetworkType.MAINNET;

        public Builder() {

        }

        public Builder maxOutboundConnections(int maxOutboundConnections) {
            this.maxOutboundConnections = maxOutboundConnections;
            return this;
        }

        public Builder maxInboundConnections(int maxInboundConnections) {
            this.maxInboundConnections = maxInboundConnections;
            return this;
        }

        public Builder maxInboundConnectionsPerIp(int maxInboundConnectionsPerIp) {
            this.maxInboundConnectionsPerIp = maxInboundConnectionsPerIp;
            return this;
        }

        public Builder maxPeersToDiscoverCount(int maxPeersToDiscoverCount) {
            this.maxPeersToDiscoverCount = maxPeersToDiscoverCount;
            return this;
        }

        public Builder p2pServerListeningPort(int p2pServerListeningPort) {
            this.p2pServerListeningPort = p2pServerListeningPort;
            return this;
        }

        public Builder peerPersistEnabled(boolean peerPersistEnabled) {
            this.peerPersistEnabled = peerPersistEnabled;
            return this;
        }

        public Builder p2pConnectionTimeout(int p2pConnectionTimeout) {
            this.p2pConnectionTimeout = p2pConnectionTimeout;
            return this;
        }

        public Builder networkType(NetworkType networkType) {
            this.networkType = networkType;
            return this;
        }

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public Network build() {
            return new Network(this);
        }
    }
}
