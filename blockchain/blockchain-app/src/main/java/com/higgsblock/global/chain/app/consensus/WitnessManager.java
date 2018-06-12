package com.higgsblock.global.chain.app.consensus;

import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.WitnessEntity;
import com.higgsblock.global.chain.app.config.AppConfig;
import com.higgsblock.global.chain.app.service.IWitnessEntityService;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Priority;
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

    @Autowired
    private BlockService blockService;

    @Autowired
    private AppConfig config;

    @Override
    public void afterPropertiesSet() throws Exception {
        initWitness();
        loadWitnessFromDb();
    }

    public void initWitness() {
        List<String> witnessAddrList = config.getWitnessAddrList();
        List<Integer> witnessSocketPortList = config.getWitnessSocketPortList();
        List<Integer> witnessHttpPortList = config.getWitnessHttpPortList();
        List<String> witnessPubkeyList = config.getWitnessPubkeyList();

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
        //List<WitnessEntity> entities = witnessService.getAll();
//        blockService.initWitness();
        List<WitnessEntity> entities = BlockService.WITNESS_ENTITY_LIST;
        if (CollectionUtils.isNotEmpty(entities)) {
            peerManager.setWitnessPeers(WitnessEntity.witnessEntity2Peer(entities));
        }
    }

    public synchronized void refresh(List<WitnessEntity> witnessEntities) {
        witnessService.addAll(witnessEntities);
    }
}