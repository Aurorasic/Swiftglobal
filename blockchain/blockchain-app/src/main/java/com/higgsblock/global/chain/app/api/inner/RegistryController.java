package com.higgsblock.global.chain.app.api.inner;

import com.higgsblock.global.chain.app.api.service.PeerRespService;
import com.higgsblock.global.chain.network.Peer;
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
    @Autowired
    private PeerRespService peerRespService;

    @RequestMapping("/peers")
    public List<Peer> peers() {
        return peerRespService.getSeedPeerList();
    }

    /**
     * Report peer.
     *
     * @param peer the peer
     * @return the boolean
     */
    @RequestMapping("/report")
    public Boolean report(@RequestBody Peer peer) {
        return this.peerRespService.report(peer);
    }

    @RequestMapping("/query")
    public Peer query(@RequestParam String address) {
        Peer peer = peerRespService.getPeer(address);
        return peer == null ? new Peer() : peer;
    }
}
