package com.higgsblock.global.chain.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.dao.IWitnessRepository;
import com.higgsblock.global.chain.app.dao.entity.BlockChainInfoEntity;
import com.higgsblock.global.chain.app.dao.entity.WitnessEntity;
import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.app.net.peer.PeerManager;
import com.higgsblock.global.chain.app.service.IBlockChainInfoService;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.crypto.ECKey;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
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
public class WitnessService implements IWitnessService, InitializingBean {

    public final static List<String> WITNESS_ADDRESS_LIST = new ArrayList<>();

    /**
     * The Witness repository.
     */
    @Autowired
    private IBlockChainInfoService blockChainInfoService;
    @Autowired
    private PeerManager peerManager;

    @Override
    public void afterPropertiesSet() {
        List<Peer> list = getAllWitnessPeer();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        initWitness(list);
        loadWitnessFromDb(list);
    }

    @Override
    public List<Peer> getAllWitnessPeer() {
        return blockChainInfoService.getAllWitness();
    }

    @Override
    public boolean isWitness(String address) {
        return WITNESS_ADDRESS_LIST.contains(address);
    }

    @Override
    public int getWitnessSize() {
        return WITNESS_ADDRESS_LIST.size();
    }

    private void loadWitnessFromDb(List<Peer> list) {
        peerManager.setWitnessPeers(list);
    }

    private void initWitness(List<Peer> list) {
        list.forEach(peer -> WITNESS_ADDRESS_LIST.add(ECKey.pubKey2Base58Address(peer.getPubKey())));
    }
}