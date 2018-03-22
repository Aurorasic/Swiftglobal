package cn.primeledger.cas.global.network.socket.server;

import cn.primeledger.cas.global.config.AppConfig;
import cn.primeledger.cas.global.p2p.channel.ServerChannelInitializer;
import cn.primeledger.cas.global.utils.ThreadFactoryUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadFactory;

/**
 * Peer server listens the incoming connections.
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class SocketServer {

    private ThreadFactory factory;

    private Channel serverChannel;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ApplicationContext context;

    public SocketServer() {
        this.factory = ThreadFactoryUtils.createThreadFactory("socket-server");
    }

    // todo baizhengwen 优化锁机制

    /**
     * Start the p2p server.
     */
    public synchronized void start() {
        if (isActive()) {
            return;
        }

        ChannelInitializer initializer = new ServerChannelInitializer(context);

        try {
            bossGroup = new NioEventLoopGroup(1, factory);
            workerGroup = new NioEventLoopGroup(0, factory);

            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);

            bootstrap.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, appConfig.getConnectionTimeout());
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

            bootstrap.handler(new LoggingHandler());
            bootstrap.childHandler(initializer);

            LOGGER.info("Starting socket server");
            int socketServerPort = appConfig.getSocketServerPort();
            serverChannel = bootstrap.bind(socketServerPort).addListener(channelFuture -> {
                if (channelFuture.isSuccess()) {
                    LOGGER.info("Successfully bind local port : {}", socketServerPort);
                }

                if (channelFuture.cause() != null) {
                    LOGGER.error("Failed to bind port : {}. Exception : {}",
                            socketServerPort,
                            channelFuture.cause().getMessage());
                }
            }).sync().channel();
        } catch (Exception e) {
            LOGGER.error("Failed to start socket server", e);
        }
    }

    /**
     * Shutdown the p2p server.
     */
    public synchronized void shutdown() {
        if (isActive() && serverChannel.isOpen()) {
            try {
                serverChannel.close().sync();

                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();

                serverChannel = null;
            } catch (Exception e) {
                LOGGER.error("Failed to close the channel", e);
            }
            LOGGER.info("PeerServer shut down");
        }
    }

    /**
     * Return if the p2p server is on working.
     */
    public synchronized boolean isActive() {
        return serverChannel != null;
    }
}
