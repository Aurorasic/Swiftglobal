package cn.primeledger.cas.global.service;

import cn.primeledger.cas.global.config.AppConfig;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.network.http.client.HttpClient;
import cn.primeledger.cas.global.p2p.Peer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class PeerReqService {
    private final AppConfig appConfig;
    private final KeyPair keyPair;

    private IPeerApi api;

    @Autowired
    public PeerReqService(AppConfig appConfig, KeyPair keyPair) {
        this.appConfig = appConfig;
        this.keyPair = keyPair;

        String ip = appConfig.getRegistryCenterIp();
        int port = appConfig.getRegistryCenterPort();
        this.api = HttpClient.getApi(ip, port, IPeerApi.class);
    }

    public boolean doRegisterRequest(Peer peer) {
        try {
            Boolean result = api.register(peer).execute().body();
            return result;
        } catch (IOException e) {
            LOGGER.error("IOException: {}", e.getMessage());
            return false;
        }
    }

    public List<Peer> doGetSeedPeersRequest() {
        try {
            List<Peer> peers = api.list().execute().body();
            return peers;
        } catch (IOException e) {
            LOGGER.error("IOException: {}", e.getMessage());
            return null;
        }
    }

    public Peer doGetPeerRequest(String address) {
        try {
            Peer peer = api.get(address).execute().body();
            return peer;
        } catch (IOException e) {
            LOGGER.error("IOException: {}", e.getMessage());
            return null;
        }
    }

    public List<Peer> doGetPeerListRequest(String[] address) {
        try {
            List<Peer> peers = api.getList(address).execute().body();
            return peers;
        } catch (IOException e) {
            LOGGER.error("IOException: {}", e.getMessage());
            return null;
        }
    }

}
