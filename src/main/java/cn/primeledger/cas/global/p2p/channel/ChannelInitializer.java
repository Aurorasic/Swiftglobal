package cn.primeledger.cas.global.p2p.channel;

import cn.primeledger.cas.global.p2p.handler.ClientInboundHandler;
import cn.primeledger.cas.global.p2p.handler.FrameCodecHandler;
import cn.primeledger.cas.global.p2p.handler.MessageCodecHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Initialize the channel configuration.
 *
 * @author zhao xiaogang
 */
@ChannelHandler.Sharable
@Component
public class ChannelInitializer extends io.netty.channel.ChannelInitializer<NioSocketChannel> {
    private final static int MAX_CONNECTION = 100;
    private final static int BUFF_SIZE = 256 * 1024;

    @Autowired
    private ChannelMgr channelMgr;
    @Autowired
    private ApplicationContext context;


//    public ChannelInitializer(ApplicationContext context) {
//        this.channelMgr = context.getBean(ChannelMgr.class);
//        this.context = context;
//    }

    @Override
    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
        Channel channel = new Channel(nioSocketChannel.id().toString(), false);
        channelMgr.add(channel);
        initChannelConfig(nioSocketChannel, channel);
    }

    private void initChannelConfig(NioSocketChannel nioChannel, Channel channel) {
        final ChannelPipeline pipe = nioChannel.pipeline();

        pipe.addLast("frameHandler", new FrameCodecHandler());
        pipe.addLast("frame2messageHandler", new MessageCodecHandler());
        pipe.addLast("messageHandler", new ClientInboundHandler(context, channel));

        nioChannel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(BUFF_SIZE));
        nioChannel.config().setOption(ChannelOption.SO_RCVBUF, BUFF_SIZE);
        nioChannel.config().setOption(ChannelOption.SO_BACKLOG, MAX_CONNECTION);

        nioChannel.closeFuture().addListener(listener -> {
            channelMgr.remove(channel);
        });
    }
}
