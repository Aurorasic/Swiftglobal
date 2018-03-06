package cn.primeledger.cas.global.p2p;

import cn.primeledger.cas.global.config.AppConfig;
import cn.primeledger.cas.global.p2p.channel.Channel;
import cn.primeledger.cas.global.p2p.channel.ChannelMgr;
import cn.primeledger.cas.global.p2p.message.BaseMessage;
import cn.primeledger.cas.global.p2p.message.RegisterMessage;
import cn.primeledger.cas.global.p2p.message.RegisterWrapper;
import cn.primeledger.cas.global.utils.ExecutorServices;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author yuanjiantao
 * @date Created in 3/5/2018
 */
@Slf4j
@Component
public class RegisterCenter implements InitializingBean {

    @Autowired
    private NetworkMgr networkMgr;

    @Autowired
    private ChannelMgr channelMgr;

    @Autowired
    private PeerClient peerClient;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private PeerMgr peerMgr;

    private ExecutorService executor;

    // TODO: 3/6/2018 yuanjiantao save peers to db
    /**
     *
     */
    private ConcurrentHashMap<String, RegistryPeer> registryPeers;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.executor = ExecutorServices.newSingleThreadExecutor("register", 100);
        this.registryPeers = new ConcurrentHashMap<>();
    }

    public List<RegistryPeer> getRegistryPeers() {
        List<RegistryPeer> registryPeers1 = new ArrayList<>();
        registryPeers1.addAll(registryPeers.values());
        return registryPeers1;
    }


    /**
     * add registry peer in registryPeers
     */
    public void addRegistryPeer(RegistryPeer registryPeer) {
        registryPeers.put(registryPeer.getPubKey() + registryPeer.getIp(), registryPeer);
        LOGGER.info("register or update a registry peer :pubKey:{} : {} :{}", registryPeer.getPubKey(), registryPeer.getIp(), registryPeer.getPort());
    }

    public void sendRegistryMessage() {
        String registryCenterIp = appConfig.getRegistryCenterIp();
        int port = networkMgr.getNetwork().p2pServerListeningPort();
        Peer peer = new Peer(registryCenterIp, port);
        peerMgr.addPeer(peer);
        RegisterWrapper registerWrapper = new RegisterWrapper(appConfig.getPubKey(), peerClient.getSelf().getIp(), port);
        RegisterMessage registerMessage = new RegisterMessage(registerWrapper);
        send(peer, registerMessage);
    }

    public void send(Peer peer, BaseMessage baseMessage) {
        this.executor.submit(new SendRequest(peer, baseMessage));
    }


    public class SendRequest implements Runnable {

        private Boolean result;

        private Peer peer;

        private BaseMessage baseMessage;

        public SendRequest(Peer peer, BaseMessage baseMessage) {
            this.result = true;
            this.peer = peer;
            this.baseMessage = baseMessage;
        }

        @Override
        public void run() {
            peerMgr.addPeer(peer);
            while (result) {
                CollectionUtils.forAllDo(channelMgr.getActiveChannels(), o -> {
                    Channel channel = (Channel) o;
                    Peer peer1 = channel.getPeerNode();
                    if (peer1.equals(peer)) {
                        channel.getMessageQueue().sendMessage(baseMessage);
                        result = false;
                    }
                });
            }
        }
    }

}
