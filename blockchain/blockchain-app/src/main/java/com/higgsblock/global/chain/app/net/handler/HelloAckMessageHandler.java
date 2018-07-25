package com.higgsblock.global.chain.app.net.handler;

import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.net.connection.ConnectionManager;
import com.higgsblock.global.chain.app.net.message.HelloAck;
import com.higgsblock.global.chain.network.socket.message.IMessage;
import com.higgsblock.global.chain.app.net.peer.Peer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Component
@Slf4j
public class HelloAckMessageHandler extends BaseMessageHandler<HelloAck> {

    @Autowired
    private ConnectionManager connectionManager;

    @Override
    protected boolean valid(IMessage<HelloAck> message) {
        HelloAck hello = message.getData();
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
    protected void process(IMessage<HelloAck> message) {
        String sourceId = message.getSourceId();
        connectionManager.active(sourceId, message.getData().getPeer());
    }
}

