package com.higgsblock.global.chain.app.task;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.net.ConnectionManager;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import com.higgsblock.global.chain.network.config.PeerConstant;
import com.higgsblock.global.chain.network.socket.connection.Connection;
import com.higgsblock.global.chain.network.socket.message.PeersMessage;
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
public class PeerManageTask extends BaseTask {

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private PeerManager peerManager;

    @Override
    protected void task() {

        // get seed peers from registry center if there are less then 2 peers
        if (peerManager.count() < PeerConstant.MIN_LOCAL_PEER_COUNT) {
            peerManager.getSeedPeers();
        }

        List<Peer> peers = peerManager.shuffle(10);
        if (CollectionUtils.isEmpty(peers)) {
            return;
        }
        PeersMessage peersMessage = new PeersMessage(Lists.newLinkedList(peers));
        List<Connection> connections = connectionManager.getActivatedConnections();
        for (Connection connection : connections) {
            connection.sendMessage(peersMessage);
        }
    }

    @Override
    protected long getPeriodMs() {
        return TimeUnit.SECONDS.toMillis(30);
    }
}
