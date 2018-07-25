package com.higgsblock.global.chain.app.net.handler;

import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.network.socket.message.IMessage;
import com.higgsblock.global.chain.app.net.message.SyncPeers;
import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Component
@Slf4j
public class PeersMessageHandler extends BaseMessageHandler<SyncPeers> {

    @Autowired
    private PeerManager peerManager;

    @Override
    protected boolean valid(IMessage<SyncPeers> message) {
        SyncPeers data = message.getData();
        if (null == data) {
            return false;
        }

        LinkedList<Peer> peers = data.getPeers();
        if (CollectionUtils.isEmpty(peers)) {
            return false;
        }

        for (Peer peer : peers) {
            if (!peer.valid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void process(IMessage<SyncPeers> message) {
        LinkedList<Peer> peers = message.getData().getPeers();
        peerManager.add(peers);
    }
}

