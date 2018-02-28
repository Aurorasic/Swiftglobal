package cn.primeledger.cas.global.p2p.handler;

import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.p2p.NetworkMgr;
import cn.primeledger.cas.global.p2p.channel.Channel;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import cn.primeledger.cas.global.p2p.message.BaseMessage;
import cn.primeledger.cas.global.p2p.message.HelloMessage;
import cn.primeledger.cas.global.p2p.message.MessageQueue;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 *
 * @author zhao xiaogang
 * */
public class MessageHandler extends SimpleChannelInboundHandler<BaseMessage> {


    private Channel channel;
    private MessageQueue messageQueue;
    private Network network;
    private ChannelMgr channelMgr;

    public MessageHandler(Channel channel, NetworkMgr networkMgr) {
        messageQueue = channel.getMessageQueue();
        network = networkMgr.getNetwork();
        channelMgr = networkMgr.getChannelMgr();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //active message queue
        messageQueue.activate(ctx);

        //check the number of active channels
        if (channel.isInbound() && channelMgr.getChannelCount() >=network.maxInboundConnections()) {

        }

        if (!channel.isInbound()) {
            //send a init message to peer
            messageQueue.sendMessage(new HelloMessage());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseMessage msg) throws Exception {


    }
}
