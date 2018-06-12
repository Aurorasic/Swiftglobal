package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.SourceBlock;
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
 * @author yuanjiantao
 * @date 5/25/2018
 */
@Component("sourceBlockHandler")
@Slf4j
public class SourceBlockHandler extends BaseEntityHandler<SourceBlock> {

    @Autowired
    private WitnessService witnessService;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private KeyPair keyPair;

    @Override
    protected void process(SocketRequest<SourceBlock> request) {
        SourceBlock sourceBlock = request.getData();
        if (null != sourceBlock && null != sourceBlock.getBlock()) {
            if (BlockService.WITNESS_ADDRESS_LIST.contains(ECKey.pubKey2Base58Address(keyPair.getPubKey()))) {
                witnessService.addCandidateBlockFromMiner(sourceBlock.getBlock());
            } else {
                messageCenter.dispatchToWitnesses(sourceBlock);
            }
        }

    }

}
