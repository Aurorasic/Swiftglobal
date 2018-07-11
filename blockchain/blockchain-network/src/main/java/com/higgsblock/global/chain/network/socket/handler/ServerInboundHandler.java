package com.higgsblock.global.chain.network.socket.handler;

import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.socket.ServerConnectionHandler;
import com.higgsblock.global.chain.network.socket.connection.Connection;
import com.higgsblock.global.chain.network.socket.message.BaseMessage;
import com.higgsblock.global.chain.network.socket.message.HelloMessage;
import io.netty.channel.ChannelHandlerContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * Message handler processes the received messages from peers. The message handler can mainly
 * process two types of messages. One is P2P layer message, the other is business layer message.
 * P2P layer messages include: HELLO,HELLO_ACK,GET_PEERS,PEERS; while the business layer messages
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
public class ServerInboundHandler extends BaseInboundHandler {
    @Setter
    private ServerConnectionHandler handler = new ServerConnectionHandler();

    public ServerInboundHandler(ApplicationContext applicationContext, Connection connection) {
        super(applicationContext, connection);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseMessage msg) throws Exception {
        connection.setContext(ctx);
        super.channelRead0(ctx, msg);
    }

    /**
     * Process message received from client.
     *
     * @param msg message to handle
     * @return true if message can be processed by this node, false otherwise
     */
    @Override
    protected boolean processOneSideMessage(BaseMessage msg) {
        if (msg instanceof HelloMessage) {
            processHelloMsg((HelloMessage) msg);
            return true;
        }
        return false;
    }

    /**
     * Process hello message with client node information.
     *
     * @param message message to handle
     */
    private void processHelloMsg(HelloMessage message) {
        LOGGER.warn("Message: [{}], connection id: {}", message, connection.getId());
        Peer peer = message.getPeer();
        handler.onPeerReceived(peer, connection);
    }
}
