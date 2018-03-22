package cn.primeledger.cas.global.p2p;


import cn.primeledger.cas.global.config.AppConfig;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.p2p.channel.ChannelInitializer;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import cn.primeledger.cas.global.p2p.discover.AmazonAddrDiscovery;
import cn.primeledger.cas.global.p2p.utils.IpUtils;
import cn.primeledger.cas.global.service.PeerReqService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
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
    private final static int DISCOVER_PERIOD = 5;

    /**
     * Preferred public ip address
     */
    private String ip;
    private int port;

    private NioEventLoopGroup workerGroup;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ConcurrentMap<String, Peer> peerMap;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private ChannelInitializer channelInitializer;

    @Autowired
    private ChannelMgr channelMgr;

    @Autowired
    private PeerReqService peerReqService;

    private ScheduledExecutorService executorService;
    private ScheduledFuture ipResolveFuture;

    public PeerClient() {
        AtomicInteger atomicInteger = new AtomicInteger(1);
        workerGroup = new NioEventLoopGroup(0, r -> {
            return new Thread(r, "P2P-CLIENT-" + atomicInteger.getAndIncrement());
        });

        executorService = Executors.newSingleThreadScheduledExecutor(r -> {
            return new Thread(r, "P2P-CLIENT-" + atomicInteger.getAndIncrement());
        });
    }

    public void start() {
        LOGGER.info("Starting peer client");

        this.ip = IpUtils.getLocalIntranetIp(); //Initialize with the intranet address
        this.port = appConfig.getSocketServerPort();

        if (StringUtils.isEmpty(appConfig.getClientPublicIp())) {
            // todo baizhengwen 逻辑不正确
//            startResolveIp();
        }

        saveSelfToDB();
    }

    private void saveSelfToDB() {
        String address = ECKey.pubKey2Base58Address(keyPair.getPubKey());
        peerMap.put(address, getSelf());
    }

    /**
     * start resolving public IP address.
     */
    protected void startResolveIp() {
        LOGGER.info("Starting resolving public IP address");

        try {
            ipResolveFuture = executorService.scheduleAtFixedRate(
                    new AmazonAddrDiscovery(ip, context),
                    DISCOVER_DELAY,
                    DISCOVER_PERIOD,
                    TimeUnit.MINUTES);
        } catch (MalformedURLException e) {
            LOGGER.error("Start resolving IP error: {}", e.getMessage());
        }
    }

    public Peer getSelf() {
        Peer peer = new Peer();
        peer.setIp(ip);
        // todo baizhengwen 通过upnp设置端口
        peer.setSocketServerPort(port);
        peer.setHttpServerPort(appConfig.getHttpServerPort());
        peer.setPubKey(keyPair.getPubKey());
        peer.setSignature(ECKey.signMessage(ip, keyPair.getPriKey()));
        // todo baizhengwen 添加签名等信息
        return peer;
    }

    public boolean register() {
        boolean isSuccess = peerReqService.doRegisterRequest(getSelf());
        LOGGER.info("Register self result: {}", isSuccess);
        return isSuccess;
    }

    /**
     * connect to the peer node
     */
    public void connect(Peer peer) {
        if (!channelMgr.canConnect()) {
            return;
        }

        if (getSelf().equals(peer)) {
            return;
        }

        if (channelMgr.isConnected(peer.getId())) {
            return;
        }

        doConnect(peer);
    }

    public ChannelFuture doConnect(Peer peer) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);

        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, appConfig.getConnectionTimeout());

        bootstrap.remoteAddress(peer.getIp(), peer.getSocketServerPort());
        bootstrap.handler(channelInitializer);

        return bootstrap.connect().addListener(listener -> {
            if (listener.isSuccess()) {
                LOGGER.info("Successfully connect to peer address {}:{}", peer.getIp(), peer.getSocketServerPort());
                return;
            }

            if (listener.cause() != null) {
                LOGGER.error("Failed to connect to peer address {}:{}. Exception : {}",
                        peer.getIp(), peer.getSocketServerPort(), listener.cause().getMessage());
                return;
            }

            if (listener.isCancelled()) {
                LOGGER.warn("Connected to peer address {}:{} been cancelled", peer.getIp(), peer.getSocketServerPort());
            }
        });
    }
}
