package com.higgsblock.global.chain.app.api.inner;

import com.higgsblock.global.chain.app.api.vo.ConnectionInfo;
import com.higgsblock.global.chain.app.api.vo.PeerVO;
import com.higgsblock.global.chain.app.connection.ConnectionManager;
import com.higgsblock.global.chain.app.consensus.NodeManager;
import com.higgsblock.global.chain.app.service.IScoreService;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

/**
 * status info controller
 *
 * @author baizhengwen
 * @create 2018-03-17
 */
@RequestMapping("/status")
@RestController
public class StatusController {

    @Autowired
    private IScoreService scoreService;
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private ConnectionManager connectionManager;
    @Autowired
    private PeerManager peerManager;

    /**
     * query connections of current peer
     *
     * @return
     */
    @RequestMapping("/connections")
    public Object connections() {
        return connectionManager.getActivatedConnections().stream().map(connection -> {
            ConnectionInfo info = new ConnectionInfo();
            info.setId(connection.getId());
            info.setPeerId(connection.getPeerId());
            info.setIp(connection.getIp());
            info.setPort(connection.getPort());
            info.setActivated(connection.isActivated());
            info.setClient(connection.isClient());
            info.setConnectionLevel(connection.getConnectionLevel());
            return info;
        }).collect(Collectors.toList());
    }

    /**
     * query score by address
     *
     * @param address
     * @return
     */
    @RequestMapping("/score")
    public Object score(String address) {
        return scoreService.get(address);
    }

    /**
     * query miners
     *
     * @param height
     * @param preBlockHash
     * @return
     */
    @RequestMapping("/miners")
    public Object miners(long height, String preBlockHash) {
        return nodeManager.getDposGroupByHeihgt(height, preBlockHash);
    }

    /**
     * query info of current peer
     *
     * @return
     */
    @RequestMapping("/info")
    public Object info() {
        Peer self = peerManager.getSelf();
        PeerVO vo = new PeerVO();
        vo.setAddress(self.getId());
        vo.setHttpServerPort(self.getHttpServerPort());
        vo.setSocketServerPort(self.getSocketServerPort());
        vo.setIp(self.getIp());
        vo.setPubKey(self.getPubKey());
        return vo;
    }
}
