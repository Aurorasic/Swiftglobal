package cn.primeledger.cas.global.api;

import cn.primeledger.cas.global.api.service.PeerRespService;
import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    private ChannelMgr channelMgr;

    @RequestMapping("/list")
    public List<Peer> list() {
        return peerRespService.getSeedPeerList();
    }

    @RequestMapping("/neighbors")
    public List<Peer> neighbors() {
        return channelMgr.getActivePeers();
    }

    @RequestMapping("/query")
    public Peer query(@RequestParam String address) {
        Peer peer = peerRespService.getPeer(address);
        return peer == null ? new Peer() : peer;
    }

    @RequestMapping("/querylist")
    public List<Peer> queryList(@RequestParam("address[]")String[] addressArr) {
        if (addressArr == null || addressArr.length == 0) {
            return null;
        }

        return peerRespService.getPeers(Arrays.asList(addressArr));
    }

    @RequestMapping("/register")
    public ResponseEntity<Boolean> register(@RequestBody Peer peer) {
        return new ResponseEntity(peerRespService.peerRegister(peer),HttpStatus.OK);
    }
}
