package cn.primeledger.cas.global.network.socket.handler;

import cn.primeledger.cas.global.blockchain.listener.MessageCenter;
import cn.primeledger.cas.global.common.entity.UnicastMessageEntity;
import cn.primeledger.cas.global.common.event.ReceivedDataEvent;
import cn.primeledger.cas.global.config.AppConfig;
import cn.primeledger.cas.global.consensus.syncblock.ActiveConnectionEvent;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.network.Peer;
import cn.primeledger.cas.global.network.PeerManager;
import cn.primeledger.cas.global.network.socket.Client;
import cn.primeledger.cas.global.network.socket.MessageCache;
import cn.primeledger.cas.global.network.socket.connection.Connection;
import cn.primeledger.cas.global.network.socket.connection.ConnectionManager;
import cn.primeledger.cas.global.network.socket.message.*;
import com.google.common.collect.Lists;
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
    protected AppConfig appConfig;
    protected MessageCache messageCache;
    protected MessageCenter messageCenter;

    public BaseInboundHandler(ApplicationContext context, Connection connection) {
        this.connection = connection;

        this.connectionManager = context.getBean(ConnectionManager.class);
        this.peerManager = context.getBean(PeerManager.class);
        this.client = context.getBean(Client.class);
        this.keyPair = context.getBean(KeyPair.class);
        this.appConfig = context.getBean(AppConfig.class);
        this.messageCache = context.getBean(MessageCache.class);
        this.messageCenter = context.getBean(MessageCenter.class);
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
        // biz message
        else if (message instanceof BizMessage) {
            dispatchBizMsg((BizMessage) message);
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
        connectionManager.active(peer, ctx);
        sendHelloAckMessage();
        messageCenter.send(new ActiveConnectionEvent(connection));
    }

    protected void processHelloAckMsg(ChannelHandlerContext ctx, HelloAckMessage message) {
        LOGGER.warn("Message: [{}], connection id: {}", message, connection.getId());
        Peer peer = message.getPeer();
        connectionManager.active(peer, ctx);
        messageCenter.send(new ActiveConnectionEvent(connection));
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

    protected void dispatchBizMsg(BizMessage message) {
        if (messageCache.isCached(connection.getPeerId(), message.getHash())) {
            return;
        }
        LOGGER.warn("Message: [{}], connection peer id: {}", message, connection.getPeerId());
        ReceivedDataEvent event = new ReceivedDataEvent();
        UnicastMessageEntity entity = new UnicastMessageEntity();

        entity.setSourceId(connection.getPeerId());
        entity.setData(message.getData());

        event.setEntity(entity);
        messageCenter.send(event);
    }

    protected void sendHelloMessage(ChannelHandlerContext context) {
        //As a client working mode, will send a hello message to peer when connected.
        context.writeAndFlush(new HelloMessage(peerManager.getSelf()));
    }

    protected void sendHelloAckMessage() {
        connection.sendMessage(new HelloAckMessage(peerManager.getSelf()));
    }
}
