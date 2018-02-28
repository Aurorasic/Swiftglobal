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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Peer server listens the incoming connections.
 *
 * @author zhao xiaogang
 */
public class PeerServer {

    private static final Logger logger = LoggerFactory.getLogger(PeerServer.class);

    private final ThreadFactory factory;

    private Channel serverChannel;
    private NetworkMgr networkMgr;
    private Network network;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    public PeerServer(NetworkMgr networkMgr) {
        this.networkMgr = networkMgr;
        this.network = networkMgr.getNetwork();

        this.factory = new ThreadFactory() {
            AtomicInteger atomicInteger = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "P2P-SERVER-" + atomicInteger.getAndIncrement());
            }
        };
    }

    /**
     * Start the p2p server.
     */
    public void start() {
        if (isActive()) {
            return;
        }

        ChannelInitializer initializer = new ChannelInitializer(networkMgr, null);

        try {
            bossGroup = new NioEventLoopGroup(1, factory);
            workerGroup = new NioEventLoopGroup(0, factory);

            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);

            b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, networkMgr.getNetwork().p2pConnectionTimeout());

            b.handler(new LoggingHandler());
            b.childHandler(initializer);

            logger.info("Starting peer server");
            serverChannel = b.bind("localhost", network.p2pServerListeningPort()).sync().channel();
        } catch (Exception e) {
            logger.error("Failed to start peer server", e);
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
                logger.error("Failed to close the channel", e);
            }
            logger.info("PeerServer shut down");
        }
    }

    /**
     * Return if the p2p server is on working.
     */
    public boolean isActive() {
        return serverChannel != null;
    }

}
