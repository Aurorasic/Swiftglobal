package cn.primeledger.cas.global.p2p.discover;

import cn.primeledger.cas.global.config.AppConfig;
import cn.primeledger.cas.global.config.NetworkType;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.p2p.PeerMgr;
import cn.primeledger.cas.global.p2p.exception.ParseAddrException;
import cn.primeledger.cas.global.service.PeerReqService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

/**
 * The amazon public ip address discover
 *
 * @author zhao xiaogang
 */

@Slf4j
public class AmazonAddrDiscovery extends BaseAddrDiscovery implements Runnable {
    private final static String HOST = "http://checkip.amazonaws.com";
    private String ip;
    private PeerMgr peerMgr;
    private AppConfig appConfig;
    private PeerReqService service;
    private boolean hasRegistred;
    private KeyPair keyPair;

    public AmazonAddrDiscovery(String ip, ApplicationContext c) throws MalformedURLException {
        super(new URL(HOST));
        this.ip = ip;

        this.appConfig = c.getBean(AppConfig.class);
        this.peerMgr = c.getBean(PeerMgr.class);
        this.service = c.getBean(PeerReqService.class);
        keyPair = c.getBean(KeyPair.class);
    }

    @Override
    public String parse(String content) throws ParseAddrException {
        String ip = content.trim();

        if (ip.matches("(\\d{1,3}\\.){3}\\d{1,3}")) {
            return ip;
        } else {
            throw new ParseAddrException("Invalid ip address");
        }
    }

    @Override
    public void run() {
        try {
            if (appConfig.getNetworkType() == NetworkType.DEVNET.getType()) {
                doGetSeedPeers();
                if (!hasRegistred) {
                    doRegisterPeer(ip);
                }
                return;
            }

            String newIp = resolveAddress();
            LOGGER.info("Updated public IP address: {}", newIp);
            try {
                if (!ip.equals(newIp) && !InetAddress.getByName(newIp).isSiteLocalAddress()) {
                    LOGGER.info("New public IP address found: {} => {}", ip, newIp);
                    ip = newIp;

                    doGetSeedPeers();
                    if (!hasRegistred) {
                        doRegisterPeer(ip);
                    }
                }
            } catch (UnknownHostException e) {
                LOGGER.error("Returned ip is invalid: {}", e.getMessage());
            }
        } catch (IOException e) {
            LOGGER.error("Error for resolving address");
            doGetSeedPeers();
            if (!hasRegistred) {
                doRegisterPeer(ip);
            }
        } catch (ParseAddrException e) {
            LOGGER.error("Error for parsing address");
            doGetSeedPeers();
            if (!hasRegistred) {
                doRegisterPeer(ip);
            }
        }
    }

    private void doRegisterPeer(String ip) {
        String pubKey = keyPair.getPubKey();
        Peer peer = new Peer();
        peer.setIp(ip);
        peer.setSocketServerPort(appConfig.getSocketServerPort());
        peer.setHttpServerPort(appConfig.getHttpServerPort());
        peer.setPubKey(pubKey);
        String signature = ECKey.signMessage(ip, keyPair.getPriKey());
        peer.setSignature(signature);

        hasRegistred = service.doRegisterRequest(peer);

        LOGGER.info("Register peer result: {}", hasRegistred);
    }

    private void doGetSeedPeers() {
        List<Peer> peers = service.doGetSeedPeersRequest();

        LOGGER.info("get peers: {}", peers);
        if (CollectionUtils.isNotEmpty(peers)) {
            peerMgr.add(peers);
        }
    }
}
