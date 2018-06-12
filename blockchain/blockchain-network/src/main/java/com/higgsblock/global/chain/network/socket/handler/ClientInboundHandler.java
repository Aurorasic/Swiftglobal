package com.higgsblock.global.chain.network.socket.handler;

import com.higgsblock.global.chain.network.socket.connection.Connection;
import com.higgsblock.global.chain.network.socket.message.HelloMessage;
import io.netty.channel.ChannelHandlerContext;
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
 * @author zhaoxiaogang
 * @author chenjiawei
 * @date 2018-05-21
 */
@Slf4j
public class ClientInboundHandler extends BaseInboundHandler {
    public ClientInboundHandler(ApplicationContext applicationContext, Connection connection) {
        super(applicationContext, connection);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        connection.setContext(ctx);
        sendHelloMessage(ctx);
    }

    /**
     * Send information of this node to server.
     *
     * @param context message writer
     */
    private void sendHelloMessage(ChannelHandlerContext context) {
        context.writeAndFlush(new HelloMessage(peerManager.getSelf()));
    }
}
