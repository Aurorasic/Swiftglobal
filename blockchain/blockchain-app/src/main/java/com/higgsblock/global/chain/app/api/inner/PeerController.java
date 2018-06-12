package com.higgsblock.global.chain.app.api.inner;

import com.higgsblock.global.chain.app.api.service.PeerRespService;
import com.higgsblock.global.chain.app.connection.ConnectionManager;
import com.higgsblock.global.chain.network.Peer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author baizhengwen
 * @create 2018-03-17
 */
@RequestMapping("/peers")
@RestController
public class PeerController {
    @Autowired
    private PeerRespService peerRespService;

    @Autowired
    private ConnectionManager connectionManager;

    @RequestMapping("/list")
    public List<Peer> list() {
        return peerRespService.getSeedPeerList();
    }

    @RequestMapping("/neighbors")
    public List<Peer> neighbors() {
        return connectionManager.getActivatedPeers();
    }

    @RequestMapping("/query")
    public Peer query(@RequestParam String address) {
        Peer peer = peerRespService.getPeer(address);
        return peer == null ? new Peer() : peer;
    }

    @RequestMapping("/querylist")
    public List<Peer> queryList(@RequestParam("address[]") String[] addressArr) {
        if (addressArr == null || addressArr.length == 0) {
            return null;
        }

        return peerRespService.getPeers(Arrays.asList(addressArr));
    }

    @RequestMapping("/register")
    public ResponseEntity<Boolean> register(@RequestBody Peer peer) {
        return new ResponseEntity(peerRespService.peerRegister(peer), HttpStatus.OK);
    }
}
