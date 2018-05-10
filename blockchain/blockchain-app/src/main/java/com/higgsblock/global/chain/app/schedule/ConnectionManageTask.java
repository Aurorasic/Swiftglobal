package com.higgsblock.global.chain.app.schedule;

import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import com.higgsblock.global.chain.network.socket.Client;
import com.higgsblock.global.chain.network.socket.connection.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @date 2018/3/23
 */
@Slf4j
@Component
public class ConnectionManageTask extends BaseTask {

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private PeerManager peerManager;

    @Autowired
    private Client client;

    @Override
    protected void task() {
        // remove inactive connection
        int inactiveConnections = connectionManager.cleanInactiveConnections();
        LOGGER.info("close {} inactive connections", inactiveConnections);

        if (!connectionManager.canConnect(true)) {
            return;
        }

        List<Peer> peers = peerManager.shuffle(10);
        if (CollectionUtils.isEmpty(peers)) {
            LOGGER.warn("no peers to connect");
            return;
        }

        //todo kongyu 2018-4-19 16:44 多次尝试连接后，最终还是连接不上，直接将该peer节点数据从数据中删除
        int newConnNum = 0;
        for (; newConnNum < peers.size(); newConnNum++) {
            /*
            1.Verify that the primary field is valid for the peer node
            2.Verify that the retries number exceeds the number of consecutive failures allowed
             */
            Peer peer = peers.get(newConnNum);
            if (null == peer) {
                LOGGER.info("peer node is null");
                continue;
            }
            if (!checkPeerParams(peer)) {
                //The peer parameter is not correct and the node needs to be removed
                //Attempts to connect retries are not correct, and the node needs to be removed
                LOGGER.info("peer params is invalid, peer_id = {}_peer_ip = {}_peer_port = {}, and remove this peer node"
                        , peer.getId()
                        , peer.getIp()
                        , peer.getSocketServerPort());
                peerManager.removePeer(peer);
                continue;
            }
            client.connect(peer);
        }
        LOGGER.info("create {} new connections", newConnNum);
    }

    public boolean checkPeerParams(Peer peer) {
        if (!peer.valid()) {
            return false;
        }

        if (peer.getRetries() < 0 || peer.getRetries() >= 5) {
            return false;
        }
        return true;
    }

    @Override
    protected long getPeriodMs() {
        return TimeUnit.SECONDS.toMillis(5);
    }
}
