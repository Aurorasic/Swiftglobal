package com.higgsblock.global.chain.app.service.impl;

import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.dao.IWitnessRepository;
import com.higgsblock.global.chain.app.dao.entity.WitnessEntity;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.network.Peer;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Witness service.
 *
 * @author liuweizhen
 * @date 2018 -05-21
 */
@Service
public class WitnessService implements IWitnessService {

    /**
     * The Witness repository.
     */
    @Autowired
    private IWitnessRepository witnessRepository;

    private static Peer witnessEntity2Peer(WitnessEntity witnessEntity) {
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
    public List<Peer> getAllWitnessPeer() {
        List<WitnessEntity> list = witnessRepository.findAll();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>(0);
        }

        List<Peer> peers = Lists.newArrayList();
        list.forEach(witnessEntity -> {
            peers.add(witnessEntity2Peer(witnessEntity));
        });
        return peers;
    }
}