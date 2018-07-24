package com.higgsblock.global.chain.app.task;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.net.message.SyncPeers;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import com.higgsblock.global.chain.network.config.PeerConstant;
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
    private PeerManager peerManager;

    @Autowired
    private MessageCenter messageCenter;

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
        SyncPeers message = new SyncPeers();
        message.setPeers(Lists.newLinkedList(peers));
        messageCenter.broadcast(message);
    }

    @Override
    protected long getPeriodMs() {
        return TimeUnit.SECONDS.toMillis(30);
    }
}
