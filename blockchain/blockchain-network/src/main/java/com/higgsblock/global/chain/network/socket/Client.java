package com.higgsblock.global.chain.network.socket;

import com.higgsblock.global.chain.common.utils.ThreadFactoryUtils;
import com.higgsblock.global.chain.network.config.PeerConfig;
import com.higgsblock.global.chain.network.socket.constants.ChannelType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
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
    private PeerConfig config;
    @Autowired
    private ChannelInitializer channelInitializer;

    public Client() {
        workerGroup = new NioEventLoopGroup(0, ThreadFactoryUtils.createThreadFactory("client"));
    }

    /**
     * Connect to remote peer.
     */
    public void connect(String ip, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);

        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectionTimeOutMs());

        bootstrap.remoteAddress(ip, port);
        bootstrap.handler(channelInitializer);

        AttributeKey<ChannelType> key = AttributeKey.valueOf("ChannelType");
        bootstrap.attr(key, ChannelType.OUT);

        LOGGER.info("Connecting to server: ip={}, port={}", ip, port);
        bootstrap.connect().addListener(channelFuture -> {
            if (channelFuture.isSuccess()) {
                LOGGER.info("Successfully connect to server: ip={}, port={}", ip, port);
                return;
            }
            if (channelFuture.cause() != null) {
                LOGGER.info("Failed to connect to server: ip={}, port={}. Exception: {}", ip, port, channelFuture.cause().getMessage());
                return;
            }
            if (channelFuture.isCancelled()) {
                LOGGER.warn("Connecting to server(ip={}, port={}) has been cancelled", ip, port);
            }
        });
    }
}
