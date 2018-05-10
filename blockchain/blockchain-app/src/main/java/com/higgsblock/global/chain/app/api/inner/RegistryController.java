package com.higgsblock.global.chain.app.api.inner;

import com.higgsblock.global.chain.app.api.service.PeerRespService;
import com.higgsblock.global.chain.network.Peer;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private PeerRespService peerRespService;

    @RequestMapping("/peers")
    public List<Peer> peers() {
        return peerRespService.getSeedPeerList();
    }
}
