package cn.primeledger.cas.global.p2p.handler;

import cn.primeledger.cas.global.Application;
import cn.primeledger.cas.global.common.entity.UnicastMessageEntity;
import cn.primeledger.cas.global.common.event.ReceivedDataEvent;
import cn.primeledger.cas.global.config.AppConfig;
import cn.primeledger.cas.global.config.NetworkType;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.p2p.PeerClient;
import cn.primeledger.cas.global.p2p.PeerMgr;
import cn.primeledger.cas.global.p2p.channel.Channel;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import cn.primeledger.cas.global.p2p.message.*;
import cn.primeledger.cas.global.p2p.utils.IpUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>Message handler processes the received messages from peers. The message handler can mainly
 * process two types of messages. One is P2P layer message, the other is business layer message.
 * P2P layer messages include: HELLO,HELLO_ACK,GET_PEERS,PEERS; while the business layer messages
 * merely includes: BIZ_MSG.
 * </p>
 * <p>
 * <p>
 * The P2P layer messages processed at the P2P layer at receiving. But the business messages be
 * transmit only, the P2P layer wont process them.
 * </p>
 *
 * @author zhao xiaogang
 */
@Slf4j
public class BaseInboundHandler extends SimpleChannelInboundHandler<BaseMessage> {
    protected Channel channel;
    protected ChannelMgr channelMgr;
    protected PeerMgr peerMgr;
    protected PeerClient peerClient;
    protected KeyPair keyPair;
    protected AppConfig appConfig;

    public BaseInboundHandler(ApplicationContext c, Channel channel) {
        this.channel = channel;

        this.channelMgr = c.getBean(ChannelMgr.class);
        this.peerMgr = c.getBean(PeerMgr.class);
        this.peerClient = c.getBean(PeerClient.class);
        this.keyPair = c.getBean(KeyPair.class);
        this.appConfig = c.getBean(AppConfig.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseMessage msg) throws Exception {
        MessageType messageType = MessageType.of(msg.getCmd());
        String channelId = ctx.channel().id().toString();
        Channel channel = channelMgr.getChannelById(channelId);

        LOGGER.warn("Message: [{}], channel id: {}", messageType, channel.getId());

        switch (MessageType.of(msg.getCmd())) {
            case HELLO:
                processHelloMsg(ctx, (HelloMessage) msg);
                break;
            case HELLO_ACK:
                processHelloAckMsg(ctx, (HelloAckMessage) msg);
                break;
            case GET_PEERS:
                processGetPeersMsg();
                break;
            case PEERS:
                processPeersMsg((PeersMessage) msg);
                break;
            case BIZ_MSG:
                dispatchBizMsg((BizMessage) msg);
                break;
            default:
                ctx.fireChannelRead(msg);
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("Exception, channel id={}, exception={}", ctx.channel().id(), cause);
        ctx.close();
    }

    protected void processHelloMsg(ChannelHandlerContext ctx, HelloMessage msg) {
        HelloMessage.Wrapper wrapper = msg.getData();
        Peer peer = new Peer();
        peer.setIp(wrapper.getIp());
        peer.setSocketServerPort(wrapper.getPort());
        peer.setPubKey(wrapper.getPubKey());

        boolean hasError = false;
        if (channelMgr.isConnected(channel.getPeerId())) {
            hasError = true;
        }

        if (wrapper.invalidParams() || wrapper.invalidSignature()) {
            hasError = true;
        }

        if (!hasError) {
            channel.onActive(peer, ctx);
            sendHelloAckMessage(); //Send back message
        } else {
            LOGGER.warn("Disconnecting channel");
            ctx.close();
        }
    }

    protected void processHelloAckMsg(ChannelHandlerContext ctx, HelloAckMessage msg) {
        HelloAckMessage.Wrapper wrapper = msg.getData();

        if (wrapper.validParams() && wrapper.validSignature()) {
            Peer peer = new Peer();
            peer.setIp(wrapper.getIp());
            peer.setSocketServerPort(wrapper.getPort());
            peer.setPubKey(wrapper.getPubKey());
            channel.onActive(peer, ctx);
        } else {
            LOGGER.warn("Disconnecting channel");
            ctx.close();
        }
    }

    protected void processGetPeersMsg() {
        int port = channel.getPeer().getSocketServerPort();

        List<Peer> peers = channelMgr.getActivePeers().stream()
                .filter(peer -> (peer.getSocketServerPort() != port))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(peers)) {
            PeersMessage peersMessage = new PeersMessage(Lists.newLinkedList(peers));
            channel.sendMessage(peersMessage);
            LOGGER.info("Send peers: {}", JSON.toJSONString(peers));
        }
    }

    protected void processPeersMsg(PeersMessage msg) {
        List<Peer> peers = msg.getData();
        int selfPort = appConfig.getSocketServerPort();

        if (CollectionUtils.isNotEmpty(peers)) {
            if (appConfig.getNetworkType() == NetworkType.TESTNET.getType()) {
                peers = peers.stream()
                        .filter(peer -> (peer.getSocketServerPort() != selfPort))
                        .collect(Collectors.toList());
            } else {
                String selfIp = peerClient.getSelf().getIp();
                Predicate<Peer> predicate = peer -> ((peer.getSocketServerPort() != selfPort)
                        && !selfIp.equals(peer.getIp()));
                peers = peers.stream()
                        .filter(predicate)
                        .collect(Collectors.toList());
            }

            peerMgr.add(peers);
        }
    }

    protected void dispatchBizMsg(BizMessage msg) {
        BizMessage.Wrapper wrapper = msg.getData();
        String sourceId = channel.getPeerId();
        if (!channelMgr.shouldDispatch(wrapper.getData(), sourceId)) {
            LOGGER.warn("Duplicated business message: {}", msg.getCmd());
            return;
        }
        channelMgr.putMessageCached(wrapper.getData(), sourceId);

        ReceivedDataEvent event = new ReceivedDataEvent();
        UnicastMessageEntity entity = new UnicastMessageEntity();

        entity.setSourceId(sourceId);
        entity.setType(wrapper.getType());
        entity.setVersion(wrapper.getVersion());
        entity.setData(wrapper.getData());

        event.setEntity(entity);
        Application.EVENT_BUS.post(event);
    }

    protected void sendHelloMessage(ChannelHandlerContext context) {
        //As a client working mode, will send a hello message to peer when connected.
        HelloMessage.Wrapper wrapper = new HelloMessage.Wrapper();

        if (appConfig.getNetworkType() == NetworkType.DEVNET.getType()) {
            wrapper.setIp(IpUtils.getLocalIntranetIp());
        } else {
            wrapper.setIp(peerClient.getSelf().getIp());
        }

        wrapper.setPort(appConfig.getSocketServerPort());
        wrapper.setPubKey(keyPair.getPubKey());

        String signature = ECKey.signMessage(wrapper.getIp(), keyPair.getPriKey());
        wrapper.setSignature(signature);

        context.writeAndFlush(new HelloMessage(wrapper));
    }

    protected void sendHelloAckMessage() {
        HelloAckMessage.Wrapper wrapper = new HelloAckMessage.Wrapper();
        if (appConfig.getNetworkType() == NetworkType.TESTNET.getType()) {
            wrapper.setIp(IpUtils.getLocalIntranetIp());
        } else {
            wrapper.setIp(peerClient.getSelf().getIp());
        }

        wrapper.setPort(appConfig.getSocketServerPort());//send the listen port
        wrapper.setPubKey(keyPair.getPubKey());

        String signature = ECKey.signMessage(wrapper.getIp(), keyPair.getPriKey());
        wrapper.setSignature(signature);

        channel.sendMessage(new HelloAckMessage(wrapper));
    }
}
