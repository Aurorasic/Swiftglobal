package cn.primeledger.cas.global.p2p.channel;

import cn.primeledger.cas.global.p2p.NetworkMgr;
import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.p2p.handler.Frame2MessageHandler;
import cn.primeledger.cas.global.p2p.handler.FrameHandler;
import cn.primeledger.cas.global.p2p.handler.MessageHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * Initialize the channel configuration.
 *
 * @author zhao xiaogang
 */
public class ChannelInitializer extends io.netty.channel.ChannelInitializer<NioSocketChannel> {
    private final static int MAX_CONNECTION = 100;
    private final static int BUFF_SIZE = 256 * 1024;

    private NetworkMgr networkMgr;
    private ChannelMgr channelMgr;
    private Peer peerNode;

    public ChannelInitializer(NetworkMgr networkMgr, Peer peerNode) {
        this.networkMgr = networkMgr;
        this.channelMgr = networkMgr.getChannelMgr();
        this.peerNode = peerNode;
    }

    public boolean isInbound() {
        return this.peerNode == null;
    }

    @Override
    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {

        /**
         * If at the inbound state, the ChannelInitializer should work for a p2p server. So the address of local channel
         * will use the remoteAddress from {@link NioSocketChannel}.Otherwise, which will use the local address.
         */
        InetSocketAddress socketAddress = isInbound() ? nioSocketChannel.remoteAddress() : peerNode.getAddress();

        Channel channel = new Channel(socketAddress, isInbound());
        channelMgr.add(channel);

        initChannelConfig(nioSocketChannel, channel);
    }

    private void initChannelConfig(NioSocketChannel nioChannel, Channel channel) {
        final ChannelPipeline pipe = nioChannel.pipeline();

        pipe.addLast("frameHandler", new FrameHandler());
        pipe.addLast("frame2messageHandler", new Frame2MessageHandler());
        pipe.addLast("messageHandler", new MessageHandler(channel, networkMgr));

        nioChannel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(BUFF_SIZE));
        nioChannel.config().setOption(ChannelOption.SO_RCVBUF, BUFF_SIZE);
        nioChannel.config().setOption(ChannelOption.SO_BACKLOG, MAX_CONNECTION);

        nioChannel.closeFuture().addListener(listener -> {
            channelMgr.remove(channel);
        });
    }

}
