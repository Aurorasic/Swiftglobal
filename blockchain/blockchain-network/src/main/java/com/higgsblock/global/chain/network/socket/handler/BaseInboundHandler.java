package com.higgsblock.global.chain.network.socket.handler;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import com.higgsblock.global.chain.network.socket.MessageCache;
import com.higgsblock.global.chain.network.socket.connection.Connection;
import com.higgsblock.global.chain.network.socket.event.ReceivedDataEvent;
import com.higgsblock.global.chain.network.socket.message.BaseMessage;
import com.higgsblock.global.chain.network.socket.message.PeersMessage;
import com.higgsblock.global.chain.network.socket.message.StringMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * Message handler processes the received messages from peers. The message handler can mainly
 * process two types of messages. One is P2P layer message, the other is business layer message.
 * P2P layer messages include: HELLO, HELLO_ACK, GET_PEERS, PEERS; while the business layer messages
 * merely includes: BIZ_MSG.
 * <br>
 * <br>
 * The P2P layer messages processed at the P2P layer at receiving. But the business messages be
 * transmit only, the P2P layer wont process them.
 *
 * @author chenjiawei
 * @date 2018-05-22
 */
@Slf4j
public class BaseInboundHandler extends SimpleChannelInboundHandler<BaseMessage> {
    protected PeerManager peerManager;
    protected MessageCache messageCache;
    protected EventBus eventBus;

    /**
     * Connection the channel belongs to.
     */
    protected Connection connection;

    public BaseInboundHandler(ApplicationContext applicationContext, Connection connection) {
        this.peerManager = applicationContext.getBean(PeerManager.class);
        this.messageCache = applicationContext.getBean(MessageCache.class);
        this.eventBus = applicationContext.getBean(EventBus.class);
        this.connection = connection;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseMessage msg) throws Exception {
        if (msg == null || !msg.valid()) {
            return;
        }

        if (msg instanceof StringMessage) {
            processStringMsg((StringMessage) msg);
            return;
        }
        if (msg instanceof PeersMessage) {
            processPeersMsg((PeersMessage) msg);
            return;
        }

        if (processOneSideMessage(msg)) {
            return;
        }
        LOGGER.warn("Unknown message: [{}], connection peer id: {}", msg, connection.getPeerId());
    }

    /**
     * Process message only allowed to send to client or server, not to either.
     *
     * @param msg message to handle
     * @return true if message is handled in this method, false otherwise
     */
    protected boolean processOneSideMessage(BaseMessage msg) {
        return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Exception: [{}], connection id: {}", cause, connection.getId());

        if (!ctx.channel().isActive()) {
            LOGGER.warn("Disconnected from: " + ctx.channel().remoteAddress());
        }

        ctx.close();
    }

    private void processStringMsg(StringMessage message) {
        if (messageCache.isCached(connection.getPeerId(), message.getHash())) {
            return;
        }

        LOGGER.info("Message: [{}], connection peer id: {}", message, connection.getPeerId());
        ReceivedDataEvent event = new ReceivedDataEvent();
        event.setSourceId(connection.getPeerId());
        event.setContent(message.getContent());

        eventBus.post(event);
    }

    private void processPeersMsg(PeersMessage message) {
        LOGGER.info("Message: [{}], connection peer id: {}", message, connection.getPeerId());
        List<Peer> peers = message.getPeers();
        peerManager.add(peers);
    }
}
