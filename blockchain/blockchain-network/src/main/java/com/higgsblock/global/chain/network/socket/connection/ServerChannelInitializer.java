package com.higgsblock.global.chain.network.socket.connection;

import com.higgsblock.global.chain.network.socket.ServerConnectionHandler;
import com.higgsblock.global.chain.network.socket.handler.MessageCodecHandler;
import com.higgsblock.global.chain.network.socket.handler.ServerInboundHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Initialize the channel configuration.
 *
 * @author chenjiawei
 * @date 2018-05-22
 */
@ChannelHandler.Sharable
@Component
@Slf4j
public class ServerChannelInitializer extends BaseChannelInitializer {
    @Autowired
    private ApplicationContext context;

    @Setter
    private ServerConnectionHandler handler = new ServerConnectionHandler();

    @Override
    protected void initChannel(NioSocketChannel channel) {
        Connection connection = handler.onChannelInitial(channel);
        if (connection == null) {
            channel.close();
            return;
        }

        ChannelPipeline pipe = channel.pipeline();
        pipe.addFirst("messageCodecHandler", new MessageCodecHandler());
        ServerInboundHandler serverInboundHandler = new ServerInboundHandler(context, connection);
        serverInboundHandler.setHandler(handler);
        pipe.addLast("messageHandler", serverInboundHandler);

        channel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(BUFF_SIZE));
        channel.config().setOption(ChannelOption.SO_RCVBUF, BUFF_SIZE);
        channel.config().setOption(ChannelOption.SO_BACKLOG, MAX_CONNECTION);

        channel.closeFuture().addListener(channelFuture -> handler.onChannelClosed(connection));
    }
}
