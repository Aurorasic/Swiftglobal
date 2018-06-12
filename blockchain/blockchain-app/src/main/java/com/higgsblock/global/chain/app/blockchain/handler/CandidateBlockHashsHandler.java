package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.CandidateBlockHashs;
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
@Component("candidateBlockHashsHandler")
@Slf4j
public class CandidateBlockHashsHandler extends BaseEntityHandler<CandidateBlockHashs> {

    @Autowired
    private WitnessService witnessService;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private KeyPair keyPair;

    @Override
    protected void process(SocketRequest<CandidateBlockHashs> request) {
        CandidateBlockHashs data = request.getData();
        LOGGER.info("receive candidateBlockHash {}", data);
        if (data == null || !data.valid()) {
            return;
        }
        String witnessAddress = ECKey.pubKey2Base58Address(data.getPubKey());
        if (!BlockService.WITNESS_ADDRESS_LIST.contains(witnessAddress)) {
            LOGGER.warn("Received request data is not from witness {}", witnessAddress);
            return;
        }
        if (BlockService.WITNESS_ADDRESS_LIST.contains(ECKey.pubKey2Base58Address(keyPair.getPubKey()))) {
            witnessService.setBlockHashsFromWitness(data);
        } else {
            messageCenter.dispatchToWitnesses(data);
        }
    }

}
