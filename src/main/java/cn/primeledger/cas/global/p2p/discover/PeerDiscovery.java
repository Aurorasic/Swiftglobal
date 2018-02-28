package cn.primeledger.cas.global.p2p.discover;

import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.p2p.exception.PeerDiscoveryException;

import java.util.List;

/**
 * PeerDiscovery is responsible for finding addresses of other nodes in the cas P2P network.
 *
 * @author zhao xiaogang
 */
public interface PeerDiscovery {
    /**
     * Returns a list of addresses.
     *
     * @return list of peers
     * @throws PeerDiscoveryException
     */
    List<Peer> getPeers() throws PeerDiscoveryException;

    /**
     * Shutdown discovery in progress.
     */
    void shutdown();
}
