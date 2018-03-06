package cn.primeledger.cas.global.p2p;


import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.p2p.channel.ChannelInitializer;
import cn.primeledger.cas.global.p2p.discover.AmazonAddrDiscovery;
import cn.primeledger.cas.global.p2p.utils.IpUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>A PeerClient handles the low-level communication with a cas globe peer. Which launches the connection
 * to the peer node initiatively </p>
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class PeerClient {

    private final static int DISCOVER_DELAY = 0;
    private final static int DISCOVER_PERIOD = 60;

    /**
     * Preferred public ip address
     */
    private String ip;
    private int port;

    private NioEventLoopGroup workerGroup;
    private Network network;

    @Autowired
    private NetworkMgr networkMgr;

    private ScheduledExecutorService executorService;
    private ScheduledFuture ipResolveFuture;

    public void start() {
        LOGGER.info("Starting peer client");

        this.network = networkMgr.getNetwork();
        this.ip = network.p2pClientPublicIp().orElse(IpUtils.getLocalIntranetIp());
        this.port = network.p2pServerListeningPort();

        AtomicInteger atomicInteger = new AtomicInteger(1);
        workerGroup = new NioEventLoopGroup(0, r -> {
            return new Thread(r, "P2P-CLIENT-" + atomicInteger.getAndIncrement());
        });

        executorService = Executors.newSingleThreadScheduledExecutor(r -> {
            return new Thread(r, "P2P-CLIENT-" + atomicInteger.getAndIncrement());
        });

        if (!network.p2pClientPublicIp().isPresent()) {
            startResolveIp();
        }
    }

    /**
     * start resolving public IP address.
     */
    protected void startResolveIp() {
        LOGGER.info("Starting resolving public IP address");

        try {
            ipResolveFuture = executorService.scheduleAtFixedRate(
                    new AmazonAddrDiscovery(ip),
                    DISCOVER_DELAY,
                    DISCOVER_PERIOD,
                    TimeUnit.SECONDS);
        } catch (MalformedURLException e) {
            LOGGER.error("Start resolving IP error: {}", e.getMessage());
        }
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
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, network.p2pConnectionTimeout());

        b.remoteAddress(peer.getIp(), peer.getPort());
        b.handler(initializer);

        return b.connect().addListener(listener->{
            if (listener.isSuccess()) {
                LOGGER.info("Successfully connect to peer address {}:{}", peer.getIp(), peer.getPort());
            }

            if (listener.cause() != null) {
                LOGGER.error("Failed to connect to peer address {}:{}. Exception : {}",
                        peer.getIp(), peer.getPort(), listener.cause().getMessage());
            }
        });
    }

    /**
     * Shutdown the p2p client.
     */
    public void shutdown() {
        LOGGER.info("Shutting down peer client...");

        workerGroup.shutdownGracefully();
        workerGroup.terminationFuture().syncUninterruptibly();

        ipResolveFuture.cancel(true);
    }


}
