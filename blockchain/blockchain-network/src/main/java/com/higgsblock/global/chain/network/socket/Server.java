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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadFactory;

/**
 * The server get started to listen specific port, and accept incoming connections.
 *
 * @author chenjiawei
 * @date 2018-05-21
 */
@Component
@Slf4j
public class Server {
    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    private Channel serverChannel;

    @Autowired
    private PeerConfig config;

    @Autowired
    private ServerChannelInitializer serverChannelInitializer;

    @Setter
    private ServerConnectionHandler handler = new ServerConnectionHandler();

    /**
     * Start as p2p server.
     */
    public synchronized void start() {
        if (isActive()) {
            return;
        }

        try {
            ThreadFactory factory = ThreadFactoryUtils.createThreadFactory("socket-server");
            bossGroup = new NioEventLoopGroup(1, factory);
            workerGroup = new NioEventLoopGroup(0, factory);

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);

            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectionTimeOutMs());
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

            bootstrap.handler(new LoggingHandler());
            serverChannelInitializer.setHandler(handler);
            bootstrap.childHandler(serverChannelInitializer);

            LOGGER.info("Starting socket server");
            int socketServerPort = config.getSocketPort();
            serverChannel = bootstrap.bind(socketServerPort).addListener(channelFuture -> {
                if (channelFuture.isSuccess()) {
                    LOGGER.info("Successfully bind local port : {}", socketServerPort);
                    return;
                }
                if (channelFuture.cause() != null) {
                    LOGGER.error("Failed to bind port : {}. Exception : {}", socketServerPort, channelFuture.cause().getMessage());
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
            LOGGER.info("Server shutdown");
        }
    }

    /**
     * Return true if the p2p server is on working, false otherwise.
     */
    public synchronized boolean isActive() {
        return serverChannel != null;
    }
}
