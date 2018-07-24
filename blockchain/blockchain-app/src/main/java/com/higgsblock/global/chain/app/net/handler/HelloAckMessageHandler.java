package com.higgsblock.global.chain.app.net.handler;

import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.net.ConnectionManager;
import com.higgsblock.global.chain.app.net.message.HelloAck;
import com.higgsblock.global.chain.network.Peer;
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
    protected boolean check(SocketRequest<HelloAck> request) {
        HelloAck hello = request.getData();
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
    protected void process(SocketRequest<HelloAck> request) {
        String sourceId = request.getSourceId();
        connectionManager.active(sourceId, request.getData().getPeer());
    }
}

