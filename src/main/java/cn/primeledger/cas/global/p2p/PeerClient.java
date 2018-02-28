package cn.primeledger.cas.global.p2p;


import cn.primeledger.cas.global.p2p.channel.ChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>A PeerClient handles the low-level communication with a cas globe peer. Which launches the connection
 * to the peer node initiatively </p>
 *
 * @author zhao xiaogang
 */

public class PeerClient {

    private static final Logger logger = LoggerFactory.getLogger(PeerClient.class);

    private String ip;
    private int port;

    private NioEventLoopGroup workerGroup;
    private NetworkMgr networkMgr;

    public PeerClient(NetworkMgr networkMgr) {
        this.networkMgr = networkMgr;
        this.ip = "localhost";
        this.port = networkMgr.getNetwork().p2pServerListeningPort();

        workerGroup = new NioEventLoopGroup(0, new ThreadFactory() {
            AtomicInteger atomicInteger = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "P2P-CLIENT-" + atomicInteger.getAndIncrement());
            }
        });
    }

    public Peer getSelf() {
        return new Peer(ip, port);
    }

    /**
     * connect to the peer node
     */
    public ChannelFuture connect(Peer peer, ChannelInitializer initializer) {
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);

        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, networkMgr.getNetwork().p2pConnectionTimeout());

        b.remoteAddress(peer.getAddress());
        b.handler(initializer);

        return b.connect();
    }

    /**
     * Shutdown the p2p client.
     */
    public void shutdown() {
        logger.info("Shutting down PeerClient...");

        workerGroup.shutdownGracefully();
        workerGroup.terminationFuture().syncUninterruptibly();
    }

}
