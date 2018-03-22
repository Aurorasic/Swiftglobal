package cn.primeledger.cas.global.consensus.sign.handler;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.common.handler.UniqueEntityHandler;
import cn.primeledger.cas.global.consensus.sign.model.WitnessSign;
import cn.primeledger.cas.global.consensus.sign.service.CollectSignService;
import cn.primeledger.cas.global.constants.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * After receiving the block from the creator, the witness will validate the block.
 * If the block is valid, it will resign the block and send it back to the creator.
 *
 * @author zhao xiaogang
 * @date 2018/3/6
 */
@Component("createSignHandler")
@Slf4j
public class WitnessSignHandler extends UniqueEntityHandler<Block> {

    @Autowired
    private BlockService blockService;

    @Autowired
    private CollectSignService collectSignService;

    @Override
    public EntityType getType() {
        return EntityType.BLOCK_COLLECT_SIGN;
    }

    @Override
    public void process(Block data, short version, String sourceId) {
        long height = data.getHeight();
        LOGGER.info("receive new request to witness the new block with height {} from {}", height, sourceId);
        Block block = collectSignService.getWitnessed(height);
        if (block != null) {
            LOGGER.info("have witness the same height block with height {}", height);
            return;
        }
        collectSignService.addWitnessed(height, data);
        if (!blockService.validBlockFromProducer(data)) {
            LOGGER.error("Validate block creator failed: {}", data);
        }
        String address1 = data.getMinerFirstPKSig().getAddress();
        List<Block> bestBatchBlocks = blockService.getBestBatchBlocks(height);

        Block existBlock = null;
        for (Block temp : bestBatchBlocks) {
            String address = temp.getMinerFirstPKSig().getAddress();
            if (StringUtils.equals(address1, address)) {
                existBlock = temp;
                break;
            }
        }
        if (existBlock != null) {
            LOGGER.info("can not pack two block in one batch with address {}", address1);
        }
        LOGGER.info("sign the block as a witness {}", height);
        WitnessSign witnessSign = collectSignService.createSign(data);
        collectSignService.sendSignToCreator(witnessSign, sourceId);
        LOGGER.info("send WitnessSign back to {}", sourceId);
    }

}
