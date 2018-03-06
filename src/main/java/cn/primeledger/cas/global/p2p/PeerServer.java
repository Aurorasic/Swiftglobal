package cn.primeledger.cas.global.p2p;

import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.p2p.channel.ChannelInitializer;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Peer server listens the incoming connections.
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class PeerServer {

    private ThreadFactory factory;

    private Channel serverChannel;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    @Autowired
    private NetworkMgr networkMgr;

    /**
     * Start the p2p server.
     */
    public void start() {
        if (isActive()) {
            return;
        }
        Network network = networkMgr.getNetwork();
        this.factory = new ThreadFactory() {
            AtomicInteger atomicInteger = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "P2P-SERVER-" + atomicInteger.getAndIncrement());
            }
        };

        ChannelInitializer initializer = new ChannelInitializer(networkMgr, null);

        try {
            bossGroup = new NioEventLoopGroup(1, factory);
            workerGroup = new NioEventLoopGroup(0, factory);

            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);

            b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, network.p2pConnectionTimeout());
            b.childOption(ChannelOption.SO_KEEPALIVE, true);

            b.handler(new LoggingHandler());
            b.childHandler(initializer);

            LOGGER.info("Starting peer server");
            serverChannel = b.bind(network.p2pServerListeningPort()).addListener(channelFuture -> {
                if (channelFuture.isSuccess()) {
                    LOGGER.info("Successfully bind local port : {}", network.p2pServerListeningPort());
                }

                if (channelFuture.cause() != null) {
                    LOGGER.error("Failed to bind port : {}. Exception : {}",
                            network.p2pServerListeningPort(),
                            channelFuture.cause().getMessage());
                }
            }).sync().channel();
        } catch (Exception e) {
            LOGGER.error("Failed to start peer server", e);
        }
    }

    /**
     * Shutdown the p2p server.
     */
    public void shutdown() {
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
    public boolean isActive() {
        return serverChannel != null;
    }
}
