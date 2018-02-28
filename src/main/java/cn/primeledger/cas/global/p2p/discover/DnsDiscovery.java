package cn.primeledger.cas.global.p2p.discover;

import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.p2p.PeerMgr;
import cn.primeledger.cas.global.p2p.exception.PeerDiscoveryException;
import com.google.common.collect.Lists;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * <p>Supports peer discovery through DNS.</p>
 * <p>Failure to resolve individual host names will not cause an Exception to be thrown.
 * However, if all hosts passed fail to resolve a PeerDiscoveryException will be thrown during getPeers().
 * </p>
 *
 * @author zhao xiaogang
 */
public class DnsDiscovery implements PeerDiscovery, Runnable {

    private String[] hostNames;

    private static final String[] DEFAULT_HOSTS = new String[]{
            "dnsseed.primeledger.io",
            "test.dnsseed.primeledger.io"
    };

    private Network network;
    private int peerListenPort;
    private PeerMgr peerMgr;

    public DnsDiscovery(PeerMgr peerManager, String[] hostNames) {
        this.peerMgr = peerManager;
        this.hostNames = hostNames;
        this.network = peerManager.network;
        this.peerListenPort = this.network.p2pServerListeningPort();
    }

    public DnsDiscovery(PeerMgr peerManager) {
        this(peerManager, DEFAULT_HOSTS);
    }

    @Override
    public List<Peer> getPeers() throws PeerDiscoveryException {
        final ArrayList<String> seeds = new ArrayList<>(Arrays.asList(hostNames));
        final List<Peer> peerList = Lists.newLinkedList();
        int maxPeersToDiscoverCount = 0;
        switch (network.type()) {
            case MAINNET:
                maxPeersToDiscoverCount = network.maxPeersToDiscoverCount();
                break;
            case DEVNET:
                peerList.add(new Peer("127.0.0.1", peerListenPort));
                return peerList;
            case TESTNET:
                peerList.add(new Peer("127.0.0.1", peerListenPort));
                return peerList;
            default:
                throw new RuntimeException();
        }

        for (final String seed : seeds) {
            try {
                InetAddress[] addresses = InetAddress.getAllByName(seed);

                for (InetAddress address : addresses) {
                    peerList.add(new Peer(address, peerListenPort));
                    if (peerList.size() >= maxPeersToDiscoverCount) {
                        return peerList;
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        if (peerList.size() == 0) {
            throw new PeerDiscoveryException("Unable to find any peers via DNS seeds...");
        } else {
            for (Peer peer : peerList) {
                peerMgr.addPeer(peer);
            }
        }

        return peerList;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void run() {
        try {
            getPeers();
        } catch (PeerDiscoveryException e) {
            e.printStackTrace();
        }
    }
}
