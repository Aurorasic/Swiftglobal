package com.higgsblock.global.chain.app.blockchain;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.dao.entity.WitnessEntity;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * @author liuweizhen
 * @date 2018-05-21
 */
@Component
@Slf4j
public class WitnessManager implements InitializingBean {

    @Autowired
    private IWitnessService witnessService;

    @Autowired
    private PeerManager peerManager;

    public static List<Peer> witnessEntity2Peer(List<WitnessEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>(0);
        }

        List<Peer> peers = Lists.newArrayList();
        list.forEach(witnessEntity -> {
            peers.add(witnessEntity2Peer(witnessEntity));
        });

        return peers;
    }

    public static Peer witnessEntity2Peer(WitnessEntity witnessEntity) {
        if (witnessEntity == null) {
            return null;
        }
        Peer peer = new Peer();
        peer.setIp(witnessEntity.getAddress());
        peer.setSocketServerPort(witnessEntity.getSocketPort());
        peer.setHttpServerPort(witnessEntity.getHttpPort());
        peer.setPubKey(witnessEntity.getPubKey());
        return peer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadWitnessFromDb();
    }

    private void loadWitnessFromDb() {
        List<WitnessEntity> entities = witnessService.getAll();
        if (CollectionUtils.isNotEmpty(entities)) {
            peerManager.setWitnessPeers(witnessEntity2Peer(entities));
        }
    }
}