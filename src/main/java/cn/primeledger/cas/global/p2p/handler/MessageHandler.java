package cn.primeledger.cas.global.p2p.handler;

import cn.primeledger.cas.global.Application;
import cn.primeledger.cas.global.common.entity.UnicastMessageEntity;
import cn.primeledger.cas.global.common.event.ReceivedDataEvent;
import cn.primeledger.cas.global.config.Network;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.p2p.*;
import cn.primeledger.cas.global.p2p.channel.Channel;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import cn.primeledger.cas.global.p2p.message.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Message handler processes the received messages from peers.
 *
 * @author zhao xiaogang
 */
@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<BaseMessage> {
    private Channel channel;
    private MessageQueue messageQueue;
    private Network network;
    private ChannelMgr channelMgr;
    private PeerMgr peerMgr;
    private PeerClient peerClient;
    private NetworkMgr networkMgr;
    private RegisterCenter registerCenter;

    public MessageHandler(Channel channel, Network network) {
        this.networkMgr = networkMgr;
        this.channel = channel;
        this.messageQueue = channel.getMessageQueue();
        this.network = network;

        ApplicationContext c = network.context();

        this.channelMgr = c.getBean(ChannelMgr.class);
        this.peerMgr = c.getBean(PeerMgr.class);
        this.peerClient = c.getBean(PeerClient.class);
        this.registerCenter = c.getBean(RegisterCenter.class);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {


        LOGGER.info("SimpleChannelInboundHandler: channel is active now");

        //active message queue
        messageQueue.activate(ctx);

        //check the number of active channels
        if (channel.isInbound() && channelMgr.getChannelCount() >= network.maxInboundConnections()) {

        }

        if (!channel.isInbound()) {
            //As a client mode, then send a hello message to peer when the connected.
            HelloWraper helloWraper = new HelloWraper(peerClient.getSelf().getIp(),
                    network.p2pServerListeningPort(), System.currentTimeMillis(), network.version());
            messageQueue.sendMessage(new HelloMessage(helloWraper));
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseMessage msg) throws Exception {
        MessageWrapper messageWrapper = messageQueue.receive(msg);
        int cmd = msg.getCmd();
        LOGGER.info("Message handler received message [{}], channel: {}",
                MessageType.of(cmd).name(), channel.getId());

        long maxDiectMemory = sun.misc.VM.maxDirectMemory();
        long directMemoryUsed = sun.misc.SharedSecrets.getJavaNioAccess().getDirectBufferPool().getMemoryUsed();
        LOGGER.warn("Checking Direct Memory Usage. Max : {}, Used  : {}", maxDiectMemory, directMemoryUsed);

        switch (MessageType.of(cmd)) {
            case HELLO:
                processHelloMsg(msg);
                break;
            case HELLO_ACK:
                processHelloAckMsg(msg);
                break;
            case PING:
                messageQueue.sendMessage(new PongMessage(msg.getEncoded()));
                break;
            case PONG:
                PongMessage pongMessage = (PongMessage) msg;
                long latency = System.currentTimeMillis() - messageWrapper.getLastTimestamp();
                // TODO: 2/27/2018 set latency
                break;
            case GET_PEERS:
                PeersMessage peersMessage = new PeersMessage(new HashSet<>(channelMgr.getActivePeers()));
                messageQueue.sendMessage(peersMessage);
                break;
            case PEERS:
                PeersMessage peersMessage1 = (PeersMessage) msg;
                Set<Peer> set = ((PeersMessage) msg).getPeers();
                peerMgr.add(set);
                break;
            case BIZ_MSG:
                BizMessage bizMessage = (BizMessage) msg;
                dispatchBizMsg(bizMessage);
                break;
            case REGISTER:
                RegisterMessage registerMessage = (RegisterMessage) msg;
                processRegisterMsg(registerMessage);
                break;
            case REGISTERVERIFY:
                RegisterVerifyMessage registerVerifyMessage = (RegisterVerifyMessage) msg;
                KeyPair keyPair = network.context().getBean(KeyPair.class);
                String randomNum = registerVerifyMessage.getRegisterVerifyWrapper().getRandomNum();
                messageQueue.sendMessage(new RegisterVerifyAckMessage(ECKey.signMessage(randomNum, keyPair.getPriKey())));
                break;
            case REGISTERVERIFYACK:
                RegisterVerifyAckMessage registerVerifyAckMessage = (RegisterVerifyAckMessage) msg;
                RegisterVerifyMessage registerVerifyMessage1 = (RegisterVerifyMessage) messageWrapper.getBaseMessage();
                RegisterVerifyWrapper registerVerifyWrapper = registerVerifyMessage1.getRegisterVerifyWrapper();
                String randomNum1 = registerVerifyWrapper.getRandomNum();
                String pubKey = registerVerifyWrapper.getPubKey();
                if (ECKey.verifySign(randomNum1, registerVerifyAckMessage.getSignature(), pubKey)) {
                    RegistryPeer registryPeer = new RegistryPeer(pubKey, registerVerifyWrapper.getIp(), registerVerifyWrapper.getPort());
                    registerCenter.addRegistryPeer(registryPeer);
                }
                break;
            default:
                ctx.fireChannelRead(msg);
                break;
        }

    }

    private void processRegisterMsg(RegisterMessage registerMessage) {
        RegisterWrapper registerWrapper = registerMessage.getRegisterWrapper();
        String pubKey = registerWrapper.getPubKey();
        String ip = registerWrapper.getIp();
        // TODO: 3/5/2018 whether to disconnect isconnected ip 
        int port = registerWrapper.getPort();
        Peer peer = new Peer(ip, port);

        RegisterVerifyWrapper registerVerifyWrapper = new RegisterVerifyWrapper(new Random().nextInt(10000) + "", pubKey, ip, port);
        RegisterVerifyMessage registerVerifyMessage = new RegisterVerifyMessage(registerVerifyWrapper);
        registerCenter.send(peer, registerVerifyMessage);
    }

    private void processHelloMsg(BaseMessage msg) {
        HelloMessage helloMessage = (HelloMessage) msg;
        Peer peer = Peer.getFromHelloWrapper(helloMessage.getHelloWraper());

        ErrorCode errorCode = null;
        if (channelMgr.isConnected(channel.getPeerIp())) {
            errorCode = ErrorCode.CONNECTED;
        }

        if (errorCode == null) {
            channelMgr.onChanneActive(channel, peer);
            HelloAckWraper wraper = new HelloAckWraper(peerClient.getSelf().getIp(),
                    network.p2pServerListeningPort(), System.currentTimeMillis());
            messageQueue.sendMessage(new HelloAckMessage(wraper));
        }
    }

    private void processHelloAckMsg(BaseMessage msg) {
        HelloAckMessage ack = (HelloAckMessage) msg;
        channelMgr.onChanneActive(channel, Peer.getFromHelloAckWrapper(ack.getHelloAckWraper()));
    }

    private void dispatchBizMsg(BizMessage bizMessage) {
        if (!channelMgr.shouldDispatch(bizMessage.getBizWapper().getData())) {
            LOGGER.info("Duplicated business message: {}", bizMessage.getCmd());
            return;
        }

        ReceivedDataEvent event = new ReceivedDataEvent();
        UnicastMessageEntity entity = new UnicastMessageEntity();

        entity.setSourceId(String.valueOf(channel.getId()));
        entity.setType(bizMessage.getBizWapper().getType());
        entity.setVersion(bizMessage.getBizWapper().getVersion());
        entity.setData(bizMessage.getBizWapper().getData());

        event.setEntity(entity);
        Application.EVENT_BUS.post(event);
    }

}
