package com.higgsblock.global.chain.network.socket;

import com.higgsblock.global.chain.common.utils.ThreadFactoryUtils;
import com.higgsblock.global.chain.network.config.PeerConfig;
import com.higgsblock.global.chain.network.socket.connection.ServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadFactory;

/**
 * The server merely listens and holds the incoming connections. And the messages handled by handlers.
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class Server {

    private ThreadFactory factory;

    private Channel serverChannel;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    @Autowired
    private PeerConfig config;

    @Autowired
    private ServerChannelInitializer serverChannelInitializer;

    public Server() {
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

        try {
            bossGroup = new NioEventLoopGroup(1, factory);
            workerGroup = new NioEventLoopGroup(0, factory);

            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);

            bootstrap.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectionTimeOutMs());
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

            bootstrap.handler(new LoggingHandler());
            bootstrap.childHandler(serverChannelInitializer);

            LOGGER.info("Starting socket server");
            int socketServerPort = config.getSocketPort();
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
                LOGGER.error("Failed to close the connection", e);
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
