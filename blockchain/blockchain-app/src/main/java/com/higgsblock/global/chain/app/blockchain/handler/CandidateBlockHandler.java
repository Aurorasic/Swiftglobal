package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.CandidateBlock;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.app.consensus.sign.service.WitnessService;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhaoxiaogang
 * @date 2018/2/28
 */
@Component("candidateBlockHandler")
@Slf4j
public class CandidateBlockHandler extends BaseEntityHandler<CandidateBlock> {

    @Autowired
    private WitnessService witnessService;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private KeyPair keyPair;

    @Override
    protected void process(SocketRequest<CandidateBlock> request) {
        CandidateBlock data = request.getData();
        String sourceId = request.getSourceId();
        LOGGER.info("receive CandidateBlock from address {} and data {}", sourceId, data);
        if (data == null || !data.valid()) {
            LOGGER.warn("Received request data is invalid {}", data);
            return;
        }
        String witnessAddress = ECKey.pubKey2Base58Address(data.getPubKey());
        if (!BlockService.WITNESS_ADDRESS_LIST.contains(witnessAddress)) {
            LOGGER.warn("Received request data is not from witness {}", witnessAddress);
            return;
        }
        if (BlockService.WITNESS_ADDRESS_LIST.contains(ECKey.pubKey2Base58Address(keyPair.getPubKey()))) {
            witnessService.setBlocksFromWitness(ECKey.pubKey2Base58Address(data.getPubKey()), data);
        } else {
            messageCenter.dispatchToWitnesses(data);
        }
    }

}
