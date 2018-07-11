package com.higgsblock.global.chain.network.socket;

import com.higgsblock.global.chain.common.utils.ThreadFactoryUtils;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.config.PeerConfig;
import com.higgsblock.global.chain.network.socket.connection.ClientChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The client launches the connection to the peer node. Usually, it will connect to the peer be successful unless
 * which connected with peers already or the peer is self or the out bound connections reach to the maximum. All the
 * connections are long-period connections.
 *
 * @author chenjiawei
 * @date 2018-05-21
 */
@Component
@Slf4j
public class Client {
    private NioEventLoopGroup workerGroup;

    @Autowired
    private PeerConfig peerConfig;

    @Autowired
    private ClientChannelInitializer clientChannelInitializer;

    @Setter
    private ClientConnectionHandler handler = new ClientConnectionHandler();

    public Client() {
        workerGroup = new NioEventLoopGroup(0, ThreadFactoryUtils.createThreadFactory("client"));
    }

    /**
     * Connect to remote peer.
     */
    public ChannelFuture connect(Peer peer) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);

        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, peerConfig.getConnectionTimeOutMs());

        bootstrap.remoteAddress(peer.getIp(), peer.getSocketServerPort());
        clientChannelInitializer.setHandler(handler);
        bootstrap.handler(clientChannelInitializer);

        AttributeKey<Peer> key = AttributeKey.valueOf("peer");
        bootstrap.attr(key, peer);

        LOGGER.info("Connecting to peer {}, address {}", peer.getId(), peer.getSocketAddress());
        return bootstrap.connect().addListener(channelFuture -> {
            if (channelFuture.isSuccess()) {
                handler.onSuccess(peer);
                LOGGER.info("Successfully connect to peer {}, address {}", peer.getId(), peer.getSocketAddress());
                return;
            }
            if (channelFuture.cause() != null) {
                handler.onCause(peer);
                LOGGER.info("Failed to connect to peer {}, address {}. Exception: {}",
                        peer.getId(), peer.getSocketAddress(), channelFuture.cause().getMessage());
                return;
            }
            if (channelFuture.isCancelled()) {
                handler.onCancel(peer);
                LOGGER.warn("Connecting to peer {}, address {} has been cancelled", peer.getId(), peer.getSocketAddress());
            }
        });
    }
}
