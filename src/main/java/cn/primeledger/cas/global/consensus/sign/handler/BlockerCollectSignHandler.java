package cn.primeledger.cas.global.consensus.sign.handler;

import cn.primeledger.cas.global.common.handler.BroadcastEntityHandler;
import cn.primeledger.cas.global.consensus.sign.WitnessManager;
import cn.primeledger.cas.global.consensus.sign.model.WitnessSign;
import cn.primeledger.cas.global.constants.EntityType;
import cn.primeledger.cas.global.crypto.ECKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The sign finishing handler responsible for validating signs from DPOS peers.
 * After collecting {@link BlockerCollectSignHandler#MIN_COLLECT_COUNT} the valid signs,
 * it will persist and broadcast the block data.
 *
 * @author zhao xiaogang
 * @date 2018/3/7
 */
@Component("collectSignHandler")
@Slf4j
public class BlockerCollectSignHandler extends BroadcastEntityHandler<WitnessSign> {

    @Autowired
    private WitnessManager witnessManager;

    @Override
    public EntityType getType() {
        return EntityType.BLOCK_CREATE_SIGN;
    }

    @Override
    public void process(WitnessSign data, short version, String sourceId) {
        String pubKey = data.getPubKey();
        String address = ECKey.pubKey2Base58Address(pubKey);
        if (!StringUtils.equals(address, sourceId)) {
            LOGGER.error("the witness with pubKey {} is from {}", pubKey, sourceId);
            return;
        }
        witnessManager.receiveWitness(data);
    }
}
