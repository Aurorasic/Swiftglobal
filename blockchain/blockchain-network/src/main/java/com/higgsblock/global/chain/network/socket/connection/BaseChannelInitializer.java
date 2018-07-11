package com.higgsblock.global.chain.network.socket.connection;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Channel Initializer
 *
 * @author chenjiawei
 * @date 2018-05-23
 */
public abstract class BaseChannelInitializer extends ChannelInitializer<NioSocketChannel> {
    protected static final int MAX_CONNECTION = 100;
    protected static final int BUFF_SIZE = 256 * 1024;
}
