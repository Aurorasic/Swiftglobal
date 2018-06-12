package com.higgsblock.global.chain.network.socket.connection;

import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.socket.ClientConnectionHandler;
import com.higgsblock.global.chain.network.socket.handler.ClientInboundHandler;
import com.higgsblock.global.chain.network.socket.handler.MessageCodecHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Initialize the channel configuration: Set the handlers for the pipeline and reset the
 * {@link SocketChannelConfig} for the {@link NioSocketChannel}
 *
 * @author zhaoxiaogang
 * @author chenjiawei
 * @date 2018-05-21
 */
@ChannelHandler.Sharable
@Component
@Slf4j
public class ClientChannelInitializer extends BaseChannelInitializer {
    @Autowired
    private ApplicationContext context;

    @Setter
    private ClientConnectionHandler handler = new ClientConnectionHandler();

    @Override
    protected void initChannel(NioSocketChannel channel) {
        AttributeKey<Peer> key = AttributeKey.valueOf("peer");
        Peer peer = channel.attr(key).get();
        Connection connection = handler.onChannelInitial(channel, peer);
        if (connection == null) {
            channel.close();
            return;
        }

        ChannelPipeline pipe = channel.pipeline();
        pipe.addFirst("messageCodecHandler", new MessageCodecHandler());
        pipe.addLast("messageHandler", new ClientInboundHandler(context, connection));

        channel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(BUFF_SIZE));
        channel.config().setOption(ChannelOption.SO_RCVBUF, BUFF_SIZE);
        channel.config().setOption(ChannelOption.SO_BACKLOG, MAX_CONNECTION);

        channel.closeFuture().addListener(channelFuture -> handler.onChannelClosed(connection));
    }
}
