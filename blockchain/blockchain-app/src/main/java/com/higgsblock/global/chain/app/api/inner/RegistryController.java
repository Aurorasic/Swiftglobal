package com.higgsblock.global.chain.app.api.inner;

import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @RequestMapping("/peers")
    public List<Peer> peers() {
        return peerManager.shuffle(DEFAULT_PEER_SEEDS);
    }

    /**
     * Report peer.
     *
     * @param peer the peer
     * @return the boolean
     */
    @RequestMapping("/report")
    public boolean report(@RequestBody Peer peer) {
        if (null == peer) {
            return false;
        }

        if (!peer.valid()) {
            return false;
        }

        peerManager.addOrUpdate(peer);
        return true;
    }

    @RequestMapping("/query")
    public Peer query(@RequestParam String address) {
        return peerManager.getById(address);
    }
}
