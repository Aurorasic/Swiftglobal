package com.higgsblock.global.chain.app.service;

import com.higgsblock.global.chain.app.config.AppConfig;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Deprecated
@Slf4j
@Service
public class PeerReqService {

    private IPeerApi api;

    @Autowired
    public PeerReqService(AppConfig appConfig) {
        String ip = appConfig.getRegistryCenterIp();
        int port = appConfig.getRegistryCenterPort();
        this.api = HttpClient.getApi(ip, port, IPeerApi.class);
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
}
