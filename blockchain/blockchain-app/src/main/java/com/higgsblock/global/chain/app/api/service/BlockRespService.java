package com.higgsblock.global.chain.app.api.service;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.common.event.ReceiveOrphanBlockEvent;
import com.higgsblock.global.chain.app.consensus.sign.model.WitnessSign;
import com.higgsblock.global.chain.app.consensus.sign.service.CollectSignService;
import com.higgsblock.global.chain.app.consensus.syncblock.SyncBlockService;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BlockRespService {

    private static Object lock = new Object();
    @Autowired
    private CollectSignService collectSignService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private SyncBlockService syncBlockService;
    @Autowired
    private EventBus eventBus;

    /**
     * The details steps for the witness signs the block:
     * <p>
     * Step 1: Check if the block exists in database, if not add it into;
     * Step 2: Validate the block from the block producer;
     * Step 3: Check if produces block more than two times at one round;
     * Step 4: Create sign for the block and response to the producer.
     * </p>
     */
    @Deprecated
    public WitnessSign signBlock(Block data) {
        //todo yangyi delete the class
        synchronized (lock) {
            // check prev block
            LOGGER.info("the block to sign is {}", data);
            String prevBlockHash = data.getPrevBlockHash();
            long height = data.getHeight();
            Block bestBlock = blockService.getBestBlockByHeight(height - 1);
            Block preBlock = blockService.getBlock(prevBlockHash);
            if (null == preBlock) {
                eventBus.post(new ReceiveOrphanBlockEvent(data.getHeight(), data.getHash(), ECKey.pubKey2Base58Address(data.getMinerFirstPKSig().getPubKey())));
//                syncBlockService.syncBlockByHeight(data.getHeight() - 1, ECKey.pubKey2Base58Address(data.getMinerFirstPKSig().getPubKey()));
                return null;
            }
            if (bestBlock != null && !StringUtils.equals(preBlock.getHash(), bestBlock.getHash())) {
                LOGGER.info("not base on the best block chain");
                return null;
            }

            LOGGER.info("Receive new request to witness the new block with height {} hash {}", height, data.getHash());
            Block block = collectSignService.getWitnessed(height);
            if (block != null) {
                LOGGER.info("Witness has the same block with height {}", height);
                return null;
//                if (!StringUtils.equals(block.getMinerFirstPKSig().getPubKey(), data.getMinerFirstPKSig().getPubKey())) {
//                    LOGGER.info("Witness has the same block with height {}", height);
//                    return null;
//                }
            }

            if (!blockService.validBlockFromProducer(data)) {
                LOGGER.warn("can not sign the block: {}", data);
                return null;
            }

            String address = data.getMinerFirstPKSig().getAddress();
            List<Block> bestBatchBlocks = blockService.getBestBatchBlocks(height);

            String tempAddress;
            for (Block temp : bestBatchBlocks) {
                tempAddress = temp.getMinerFirstPKSig().getAddress();
                if (StringUtils.equals(address, tempAddress)) {
                    LOGGER.info("Can not pack two block in one batch with address {}", address);
                    return null;
                }
            }

            LOGGER.info("Sign the block as a witness height={}_block={}", height, data.getHash());

            //save block witness to db
            collectSignService.addWitnessed(height, data);
            return collectSignService.createSign(data);
        }
    }
}
