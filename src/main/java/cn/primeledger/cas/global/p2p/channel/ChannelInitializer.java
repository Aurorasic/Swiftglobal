package cn.primeledger.cas.global.p2p.channel;

import cn.primeledger.cas.global.config.Network;
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
    private Network networkConfig;
    private Peer peerNode;

    public ChannelInitializer(NetworkMgr networkMgr, Peer peerNode) {
        this.channelMgr = networkMgr.getNetwork().context().getBean(ChannelMgr.class);
        this.networkConfig = networkMgr.getNetwork();
        this.networkMgr = networkMgr;
        this.peerNode = peerNode;
    }

    public boolean isInbound() {
        return this.peerNode == null;
    }

    @Override
    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
        /**
         * If the channel of the local peers is in the inbound state, the Channel Initializer will work for the
         * server mode. So the address of local channel will use the remote Address from {@link NioSocketChannel}.
         * Otherwise, it will get from {@link cn.primeledger.cas.global.p2p.discover.PeerDiscovery} .
         */
        InetSocketAddress socketAddress = isInbound() ? nioSocketChannel.remoteAddress() : peerNode.getAddress();
        boolean isDelegate = isInbound() ? false : peerNode.isDelegate();
        Channel channel = new Channel(networkConfig, socketAddress, isInbound(), isDelegate);
        channelMgr.add(channel);

        initChannelConfig(nioSocketChannel, channel);
    }

    private void initChannelConfig(NioSocketChannel nioChannel, Channel channel) {
        final ChannelPipeline pipe = nioChannel.pipeline();

        pipe.addLast("frameHandler", new FrameHandler());
        pipe.addLast("frame2messageHandler", new Frame2MessageHandler());
        pipe.addLast("messageHandler", new MessageHandler(channel, networkConfig));

        nioChannel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(BUFF_SIZE));
        nioChannel.config().setOption(ChannelOption.SO_RCVBUF, BUFF_SIZE);
        nioChannel.config().setOption(ChannelOption.SO_BACKLOG, MAX_CONNECTION);

        nioChannel.closeFuture().addListener(listener -> {
            channelMgr.remove(channel);
        });
    }

}
