package com.higgsblock.global.chain.network.socket.channel;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.network.socket.constants.ChannelType;
import com.higgsblock.global.chain.network.socket.event.ActiveChannelEvent;
import com.higgsblock.global.chain.network.socket.event.CreateChannelEvent;
import com.higgsblock.global.chain.network.socket.event.DiscardChannelEvent;
import com.higgsblock.global.chain.network.socket.handler.MessageCodecHandler;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initialize the channel configuration.
 *
 * @author chenjiawei
 * @date 2018-05-22
 */
@Component
@ChannelHandler.Sharable
@Slf4j
public class CommonChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    protected static final int MAX_CONNECTION = 100;
    protected static final int BUFF_SIZE = 256 * 1024;

    @Autowired
    private ChannelInboundHandler channelInboundHandler;
    @Autowired
    private EventBus eventBus;

    @Override
    protected void initChannel(NioSocketChannel channel) {
        AttributeKey<ChannelType> key = AttributeKey.valueOf("ChannelType");
        ChannelType channelType = channel.attr(key).get();
        if (null == channelType) {
            channelType = ChannelType.IN;
        }
        eventBus.post(new CreateChannelEvent(channel, channelType));

        ChannelPipeline pipe = channel.pipeline();
        pipe.addFirst("messageCodecHandler", new MessageCodecHandler());
        pipe.addLast("messageHandler", channelInboundHandler);

        channel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(BUFF_SIZE));
        channel.config().setOption(ChannelOption.SO_RCVBUF, BUFF_SIZE);
        channel.config().setOption(ChannelOption.SO_BACKLOG, MAX_CONNECTION);

        String channelId = channel.id().toString();
        channel.closeFuture().addListener(channelFuture -> eventBus.post(new DiscardChannelEvent(channelId)));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Channel channel = ctx.channel();
        if (null == channel) {
            return;
        }
        String channelId = channel.id().toString();
        eventBus.post(new ActiveChannelEvent(channelId));
    }
}
