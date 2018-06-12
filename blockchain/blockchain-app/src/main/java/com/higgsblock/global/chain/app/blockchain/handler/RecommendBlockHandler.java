package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.RecommendBlock;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.app.consensus.sign.service.CollectWitnessBlockService;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhaoxiaogang
 * @date 2018/2/28
 */
@Component("recommendBlockHandler")
@Slf4j
public class RecommendBlockHandler extends BaseEntityHandler<RecommendBlock> {
    public static Integer ENOUGH_SIG_NUM = 7;

    @Autowired
    private BlockService blockService;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private CollectWitnessBlockService collectWitnessBlockService;

    @Override
    protected void process(SocketRequest<RecommendBlock> request) {
        if (request == null) {
            LOGGER.warn("Received request is null");
            return;
        }
        RecommendBlock data = request.getData();
        if (data == null) {
            LOGGER.warn("Received request data is null");
            return;
        }
        if (!data.valid()) {
            LOGGER.warn("Received request data is invalid");
            return;
        }
        String witnessAddress = ECKey.pubKey2Base58Address(data.getPubKey());
        if (!BlockService.WITNESS_ADDRESS_LIST.contains(witnessAddress)) {
            LOGGER.warn("Received request data is not from witness {}", witnessAddress);
            return;
        }

        Block block = request.getData().getBlock();


        //valid recommend block
        boolean success = blockService.validRecommendBlock(block);

        LOGGER.info("collected the signed block success={}, height={}, block={}",
                success,
                block == null ? null : block.getHeight(),
                block == null ? null : block.getHash());

        if (success) {
            if (BlockService.WITNESS_ADDRESS_LIST.contains(ECKey.pubKey2Base58Address(keyPair.getPubKey()))) {
                collectWitnessBlockService.processBlockAfterCollected(block);
            } else {
                messageCenter.dispatchToWitnesses(request.getData());
            }
        }
    }

}
