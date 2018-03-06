package cn.primeledger.cas.global.consensus.sign.service;

import cn.primeledger.cas.global.Application;
import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.common.entity.CollectSignMessageEntity;
import cn.primeledger.cas.global.common.event.CollectSignEvent;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static cn.primeledger.cas.global.constants.EntityType.SIGN_BLOCK;

@Component
@Slf4j
public class CollectSignService {
    @Autowired
    private KeyPair peerKeyPair;

    public void sendSignedBlock(Block block) {
        CollectSignMessageEntity entity = new CollectSignMessageEntity();
        entity.setType(SIGN_BLOCK.getType());
        entity.setVersion(SIGN_BLOCK.getVersion());
        entity.setData(JSON.toJSONString(block));
        Application.EVENT_BUS.post(new CollectSignEvent(entity));

        LOGGER.info("Send signed block : {}", JSON.toJSONString(block));
    }
}
