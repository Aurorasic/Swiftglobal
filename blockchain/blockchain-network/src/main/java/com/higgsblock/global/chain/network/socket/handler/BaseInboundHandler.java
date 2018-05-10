package com.higgsblock.global.chain.network.socket.handler;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.crypto.KeyPair;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import com.higgsblock.global.chain.network.socket.Client;
import com.higgsblock.global.chain.network.socket.MessageCache;
import com.higgsblock.global.chain.network.socket.connection.Connection;
import com.higgsblock.global.chain.network.socket.connection.ConnectionManager;
import com.higgsblock.global.chain.network.socket.event.ActiveConnectionEvent;
import com.higgsblock.global.chain.network.socket.event.ReceivedDataEvent;
import com.higgsblock.global.chain.network.socket.message.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * <p>Message handler processes the received messages from peers. The message handler can mainly
 * process two types of messages. One is P2P layer message, the other is business layer message.
 * P2P layer messages include: HELLO,HELLO_ACK,GET_PEERS,PEERS; while the business layer messages
 * merely includes: BIZ_MSG.
 * </p>
 * <p>
 * <p>
 * The P2P layer messages processed at the P2P layer at receiving. But the business messages be
 * transmit only, the P2P layer wont process them.
 * </p>
 *
 * @author zhao xiaogang
 */
@Slf4j
public class BaseInboundHandler extends SimpleChannelInboundHandler<BaseMessage> {
    protected Connection connection;
    protected ConnectionManager connectionManager;
    protected PeerManager peerManager;
    protected Client client;
    protected KeyPair keyPair;
    protected MessageCache messageCache;
    protected EventBus eventBus;

    public BaseInboundHandler(ApplicationContext context, Connection connection) {
        this.connection = connection;
        this.connectionManager = context.getBean(ConnectionManager.class);
        this.peerManager = context.getBean(PeerManager.class);
        this.client = context.getBean(Client.class);
        this.keyPair = context.getBean(KeyPair.class);
        this.messageCache = context.getBean(MessageCache.class);
        this.eventBus = context.getBean(EventBus.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseMessage message) throws Exception {
        if (null == message || !message.valid()) {
            return;
        }

        // hello
        if (message instanceof HelloMessage) {
            processHelloMsg(ctx, (HelloMessage) message);
        }
        // hello ack
        else if (message instanceof HelloAckMessage) {
            processHelloAckMsg(ctx, (HelloAckMessage) message);
        }
        // get peers
        else if (message instanceof GetPeersMessage) {
            processGetPeersMsg((GetPeersMessage) message);
        }
        // get peers ack
        else if (message instanceof PeersMessage) {
            processPeersMsg((PeersMessage) message);
        }
        // text message
        else if (message instanceof StringMessage) {
            processStringMsg((StringMessage) message);
        }
        // other
        else {
            LOGGER.warn("unknown message: [{}], connection id: {}", message, connection.getPeerId());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("Exception, connection id=" + ctx.channel().id() + ", exception={}", cause);
        ctx.close();
    }

    protected void processHelloMsg(ChannelHandlerContext ctx, HelloMessage message) {
        LOGGER.warn("Message: [{}], connection id: {}", message, connection.getId());
        Peer peer = message.getPeer();
        if (null == peer || !peer.valid()) {
            connectionManager.close(connection);
            //todo kongyu 2018-5-2 15:36 坏节点数据删除操作
            peerManager.removePeer(peer);
            return;
        }

        connectionManager.active(peer, ctx);
        sendHelloAckMessage();
        eventBus.post(new ActiveConnectionEvent(connection));
    }

    protected void processHelloAckMsg(ChannelHandlerContext ctx, HelloAckMessage message) {
        LOGGER.warn("Message: [{}], connection id: {}", message, connection.getId());
        Peer peer = message.getPeer();
        if (null == peer || !peer.valid()) {
            connectionManager.close(connection);
            //todo kongyu 2018-5-2 15:36 坏节点数据删除操作
            peerManager.removePeer(peer);
            return;
        }
        connectionManager.active(peer, ctx);
        eventBus.post(new ActiveConnectionEvent(connection));
    }

    protected void processGetPeersMsg(GetPeersMessage message) {
        LOGGER.warn("Message: [{}], connection peer id: {}", message, connection.getPeerId());
        Integer limit = message.getSize();
        List<Peer> peers = peerManager.shuffle(limit);

        if (CollectionUtils.isNotEmpty(peers)) {
            PeersMessage peersMessage = new PeersMessage(Lists.newLinkedList(peers));
            connection.sendMessage(peersMessage);
        }
    }

    protected void processPeersMsg(PeersMessage message) {
        LOGGER.warn("Message: [{}], connection peer id: {}", message, connection.getPeerId());
        List<Peer> peers = message.getPeers();
        peerManager.add(peers);
    }

    protected void processStringMsg(StringMessage message) {
        if (messageCache.isCached(connection.getPeerId(), message.getHash())) {
            return;
        }
        LOGGER.warn("Message: [{}], connection peer id: {}", message, connection.getPeerId());
        ReceivedDataEvent event = new ReceivedDataEvent();
        event.setSourceId(connection.getPeerId());
        event.setContent(message.getContent());

        eventBus.post(event);
    }

    protected void sendHelloMessage(ChannelHandlerContext context) {
        //As a client working mode, will send a hello message to peer when connected.
        context.writeAndFlush(new HelloMessage(peerManager.getSelf()));
    }

    protected void sendHelloAckMessage() {
        connection.sendMessage(new HelloAckMessage(peerManager.getSelf()));
    }
}
