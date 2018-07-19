package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Override
    public void afterPropertiesSet() {
        List<Peer> list = witnessService.getAllWitnessPeer();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        initWitness(list);
        loadWitnessFromDb(list);
    }

    private void loadWitnessFromDb(List<Peer> list) {
        peerManager.setWitnessPeers(list);
    }

    public void initWitness(List<Peer> list) {
        list.forEach(peer -> BlockProcessor.WITNESS_ADDRESS_LIST.add(ECKey.pubKey2Base58Address(peer.getPubKey())));
    }
}