package cn.primeledger.cas.global.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * Network configuration.
 *
 * @author zhao xiaogang
 */
public class Network {
    private int maxOutboundConnections;
    private int maxInboundConnections;
    private int maxInboundConnectionsPerIp;

    private int maxPeersToDiscoverCount;
    private String p2pServerListeningIp;
    private int p2pServerListeningPort;
    private int p2pConnectionTimeout;
    private boolean peerPersistEnabled;
    private NetworkType networkType;
    private int version;
    private ApplicationContext context;
    private String p2pClientPublicIp;
    private int registryServerListeningPort;
    private int maxDelegatePeerCount;


    public Network(Builder builder) {
        maxOutboundConnections = builder.maxOutboundConnections;
        maxInboundConnections = builder.maxInboundConnections;
        maxInboundConnectionsPerIp = builder.maxInboundConnectionsPerIp;

        maxPeersToDiscoverCount = builder.maxPeersToDiscoverCount;
        p2pServerListeningIp = builder.p2pServerListeningIp;
        p2pServerListeningPort = builder.p2pServerListeningPort;
        registryServerListeningPort = builder.registryServerListeningPort;
        p2pConnectionTimeout = builder.p2pConnectionTimeout;
        peerPersistEnabled = builder.peerPersistEnabled;

        maxDelegatePeerCount = builder.maxDelegatePeerCount;

        networkType = builder.networkType;
        version = builder.version;
        context = builder.context;
    }

    public ApplicationContext context() {
        return context;
    }

    public Optional<String> p2pClientPublicIp() {
        return StringUtils.isEmpty(p2pClientPublicIp) ? Optional.empty() : Optional.of(p2pClientPublicIp);
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
        return maxInboundConnections;
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
     * Return the max count of delegate peers for each turn.
     */
    public int maxDelegatePeerCount() {
        return maxDelegatePeerCount;
    }

    /**
     * Return the p2p server's listening port number.
     */
    public int p2pServerListeningPort() {
        return p2pServerListeningPort;
    }

    /**
     * Return the p2p server's listening port number.
     */
    public int registryServerListeningPort() {
        return registryServerListeningPort;
    }

    public String p2pServerListeningIp() {
        return p2pServerListeningIp;
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


    public static enum Mode {
        PEER_MODE(1),
        DNS_MODE(2);

        private int mode;

        Mode(int mode) {
            this.mode = mode;
        }
    }

    public static class Builder {
        int maxOutboundConnections = 20;
        int maxInboundConnections = 20;
        int maxInboundConnectionsPerIp = 5;
        int maxPeersToDiscoverCount = 100;

        String p2pClientPublicIp;
        String p2pServerListeningIp = "127.0.0.1";
        int p2pServerListeningPort = 8000;
        int registryServerListeningPort = 9000;
        int p2pConnectionTimeout = 5000;

        int maxDelegatePeerCount = 10;

        boolean peerPersistEnabled;
        int version = 0;

        NetworkType networkType = NetworkType.MAINNET;
        ApplicationContext context;

        public Builder() {

        }

        public Builder context(ApplicationContext context) {
            this.context = context;
            return this;
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

        public Builder p2pServerListeningIp(String p2pServerListeningIp) {
            this.p2pServerListeningIp = p2pServerListeningIp;
            return this;
        }

        public Builder p2pClientPublicIp(String p2pClientPublicIp) {
            this.p2pClientPublicIp = p2pClientPublicIp;
            return this;
        }

        public Builder p2pServerListeningPort(int p2pServerListeningPort) {
            this.p2pServerListeningPort = p2pServerListeningPort;
            return this;
        }

        public Builder registryServerListeningPort(int registryServerListeningPort) {
            this.registryServerListeningPort = registryServerListeningPort;
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

        public Builder maxDelegatePeerCount(int maxDelegatePeerCount) {
            this.maxDelegatePeerCount = maxDelegatePeerCount;
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
