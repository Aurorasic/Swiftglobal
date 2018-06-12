package com.higgsblock.global.chain.app.api.service;

import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author zhao xiaogang
 * @date 2018/3/24
 */
@Slf4j
@Service
public class PeerRespService {
    private static int DEFAULT_RETURN_COUNT = 30;

    @Autowired
    private PeerManager peerManager;

    public boolean peerRegister(Peer peer) {
        if (peer == null) {
            LOGGER.info("Peer is null");
            return false;
        }

        if (!peer.valid()) {
            LOGGER.info("Invalid peer parameters");
            return false;
        }

        if (!peer.validSignature()) {
            LOGGER.info("Invalid signature");
            return false;
        }

        peerManager.addOrUpdate(peer);

        return true;
    }

    public Peer getPeer(String id) {
        return peerManager.getById(id);
    }

    public List<Peer> getPeers(List<String> addressList) {
        List<Peer> peers = new LinkedList<>();
        addressList.stream().forEach(address -> {
            Peer peer = getPeer(address);
            if (peer != null) {
                peers.add(peer);
            }
        });
        return peers;
    }

    /**
     * If the DB has the peers lower the {@link #DEFAULT_RETURN_COUNT},
     * will return all the value except self. Otherwise, it will return
     * {@link #DEFAULT_RETURN_COUNT} peers randomly.
     */
    public List<Peer> getSeedPeerList() {
        return peerManager.shuffle(DEFAULT_RETURN_COUNT);
    }

    /**
     * Report peer.
     *
     * @param peer the peer
     * @return the boolean
     */
    public Boolean report(Peer peer) {
        if (null == peer) {
            return false;
        }

        if(!peer.valid()){
            return false;
        }

        this.peerManager.addOrUpdate(peer);
        return true;
    }
}
