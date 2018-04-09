package cn.primeledger.cas.global.network.socket.handler;

import cn.primeledger.cas.global.network.socket.connection.Connection;
import cn.primeledger.cas.global.network.socket.message.BaseMessage;
import cn.primeledger.cas.global.network.socket.message.HelloMessage;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

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
public class ServerInboundHandler extends BaseInboundHandler {

    public ServerInboundHandler(ApplicationContext context, Connection connection) {
        super(context, connection);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseMessage msg) throws Exception {
        LOGGER.warn("Message: [{}], connection id: {}", msg, connection.getId());
        if (connection.isActivated()) {
            super.channelRead0(ctx, msg);
        } else {
            processHelloMsg(ctx, (HelloMessage) msg);
        }
    }
}
