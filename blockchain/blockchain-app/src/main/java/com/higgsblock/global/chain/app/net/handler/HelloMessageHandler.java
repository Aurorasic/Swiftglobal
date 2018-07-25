package com.higgsblock.global.chain.app.net.handler;

import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.net.connection.ConnectionManager;
import com.higgsblock.global.chain.app.net.message.Hello;
import com.higgsblock.global.chain.app.net.message.HelloAck;
import com.higgsblock.global.chain.network.socket.message.IMessage;
import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Component
@Slf4j
public class HelloMessageHandler extends BaseMessageHandler<Hello> {

    @Autowired
    private ConnectionManager connectionManager;
    @Autowired
    private MessageCenter messageCenter;
    @Autowired
    private PeerManager peerManager;

    @Override
    protected boolean valid(IMessage<Hello> message) {
        Hello hello = message.getData();
        if (null == hello) {
            return false;
        }

        Peer peer = hello.getPeer();
        if (null == peer) {
            return false;
        }

        return peer.valid();
    }

    @Override
    protected void process(IMessage<Hello> message) {
        String channelId = message.getSourceId();
        connectionManager.active(channelId, message.getData().getPeer());

        HelloAck ack = new HelloAck();
        ack.setPeer(peerManager.getSelf());
        messageCenter.handshake(channelId, ack);
    }
}

