package com.higgsblock.global.chain.app.consensus;

import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.WitnessEntity;
import com.higgsblock.global.chain.app.dao.entity.WitnessPo;
import com.higgsblock.global.chain.app.service.IWitnessEntityService;
import com.higgsblock.global.chain.crypto.ECKey;
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
    private IWitnessEntityService witnessService;

    @Autowired
    private PeerManager peerManager;


    @Override
    public void afterPropertiesSet() throws Exception {
        initWitness();
        loadWitnessFromDb();
    }

    public void initWitness() {
        List<WitnessPo> witnessPos = witnessService.getAll();
        List<String> witnessAddrList = new ArrayList<>();
        List<Integer> witnessSocketPortList = new ArrayList<>();
        List<Integer> witnessHttpPortList = new ArrayList<>();
        List<String> witnessPubkeyList = new ArrayList<>();
        for (WitnessPo witnessPo : witnessPos) {
            witnessAddrList.add(witnessPo.getAddress());
            witnessSocketPortList.add(witnessPo.getSocketPort());
            witnessHttpPortList.add(witnessPo.getHttpPort());
            witnessPubkeyList.add(witnessPo.getPubKey());
        }
        int size = witnessAddrList.size();
        for (int i = 0; i < size; i++) {
            WitnessEntity entity = getEntity(
                    witnessAddrList.get(i),
                    witnessSocketPortList.get(i),
                    witnessHttpPortList.get(i),
                    witnessPubkeyList.get(i));

            BlockService.WITNESS_ENTITY_LIST.add(entity);
        }

        BlockService.WITNESS_ENTITY_LIST.stream().forEach(entity -> {
            BlockService.WITNESS_ADDRESS_LIST.add(ECKey.pubKey2Base58Address(entity.getPubKey()));
        });
        LOGGER.info("the witness list is {}", BlockService.WITNESS_ENTITY_LIST);
    }

    private static WitnessEntity getEntity(String ip, int socketPort, int httpPort, String pubKey) {
        WitnessEntity entity = new WitnessEntity();
        entity.setSocketPort(socketPort);
        entity.setIp(ip);
        entity.setPubKey(pubKey);
        entity.setHttpPort(httpPort);

        return entity;
    }

    private synchronized void loadWitnessFromDb() {
        List<WitnessEntity> entities = BlockService.WITNESS_ENTITY_LIST;
        if (CollectionUtils.isNotEmpty(entities)) {
            peerManager.setWitnessPeers(WitnessEntity.witnessEntity2Peer(entities));
        }
    }
}