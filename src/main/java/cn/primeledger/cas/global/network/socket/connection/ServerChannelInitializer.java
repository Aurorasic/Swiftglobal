package cn.primeledger.cas.global.network.socket.connection;

import cn.primeledger.cas.global.network.socket.handler.MessageCodecHandler;
import cn.primeledger.cas.global.network.socket.handler.ServerInboundHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Initialize the channel configuration.
 *
 * @author zhao xiaogang
 */
@ChannelHandler.Sharable
@Component
public class ServerChannelInitializer extends io.netty.channel.ChannelInitializer<NioSocketChannel> {
    private final static int MAX_CONNECTION = 100;
    private final static int BUFF_SIZE = 256 * 1024;

    @Autowired
    private ConnectionManager connectionManager;
    @Autowired
    private ApplicationContext context;

    @Override
    protected void initChannel(NioSocketChannel channel) throws Exception {
        Connection connection = connectionManager.newConnection(channel.id().toString(), false);
        if (null == connection) {
            channel.close();
            return;
        }
        initChannelConfig(channel, connection);
    }

    private void initChannelConfig(NioSocketChannel channel, Connection connection) {
        final ChannelPipeline pipe = channel.pipeline();

        pipe.addFirst("messageCodecHandler", new MessageCodecHandler());
        pipe.addLast("messageHandler", new ServerInboundHandler(context, connection));

        channel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(BUFF_SIZE));
        channel.config().setOption(ChannelOption.SO_RCVBUF, BUFF_SIZE);
        channel.config().setOption(ChannelOption.SO_BACKLOG, MAX_CONNECTION);

        channel.closeFuture().addListener(listener -> connectionManager.close(connection));
    }
}
