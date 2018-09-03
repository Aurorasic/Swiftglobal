package com.higgsblock.global.chain.app.api.inner;

import com.google.common.base.Preconditions;
import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author baizhengwen
 * @create 2018-03-17
 */
@RequestMapping("/registry")
@RestController
public class RegistryController {

    private static int DEFAULT_PEER_SEEDS = 30;

    @Autowired
    private PeerManager peerManager;

    /**
     * Report peer.
     *
     * @param peer the peer
     * @return the boolean
     */
    @RequestMapping("/report")
    public List<Peer> report(@RequestBody Peer peer) {
        Preconditions.checkNotNull(peer, "peer is null");
        if (!peer.valid()) {
            throw new IllegalArgumentException(String.format("the reported peer is invalid：%s", peer.toJson()));
        }

        peerManager.add(peer);
        List<Peer> list = peerManager.shuffle(DEFAULT_PEER_SEEDS);
        return list;
    }
}