package com.higgsblock.global.chain.network.socket.handler;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.network.socket.event.ReceivedMessageEvent;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
@ChannelHandler.Sharable
@Slf4j
public class CommonInboundHandler extends SimpleChannelInboundHandler<String> {

    @Autowired
    private EventBus eventBus;

    /**
     * Connection the channel belongs to.
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        String channelId = ctx.channel().id().toString();
        if (StringUtils.isBlank(msg)) {
            LOGGER.warn("Unknown message: [{}], channel id: {}", msg, channelId);
            return;
        }

        ReceivedMessageEvent message = new ReceivedMessageEvent();
        message.setContent(msg);
        message.setChannelId(channelId);

        eventBus.post(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String channelId = ctx.channel().id().toString();
        LOGGER.error("Exception: [{}], channel id: {}", cause, channelId);

        if (!ctx.channel().isActive()) {
            LOGGER.warn("Disconnected from: " + ctx.channel().remoteAddress());
        }
        ctx.close();
    }
}
