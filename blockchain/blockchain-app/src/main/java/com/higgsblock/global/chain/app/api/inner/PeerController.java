package com.higgsblock.global.chain.app.api.inner;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author baizhengwen
 * @create 2018-03-17
 */
@RequestMapping("/peers")
@RestController
@Slf4j
public class PeerController {

    @Autowired
    private PeerManager peerManager;

    @RequestMapping("/list")
    public List<Peer> list() {
        return Lists.newLinkedList(peerManager.getPeers());
    }
}
