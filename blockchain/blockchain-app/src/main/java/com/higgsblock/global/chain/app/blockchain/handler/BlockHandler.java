package com.higgsblock.global.chain.app.blockchain.handler;

import com.higgsblock.global.chain.app.blockchain.*;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.blockchain.transaction.UTXO;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.app.consensus.sign.service.WitnessService;
import com.higgsblock.global.chain.app.consensus.syncblock.Inventory;
import com.higgsblock.global.chain.app.service.impl.BlockIdxDaoService;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component("blockHandler")
@Slf4j
public class BlockHandler extends BaseEntityHandler<Block> {

    @Autowired
    private BlockService blockService;

    @Autowired
    private BlockCacheManager blockCacheManager;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private WitnessService witnessService;

    @Autowired
    private BlockIdxDaoService blockIdxDaoService;

    @Autowired
    private PeerManager peerManager;
    @Autowired
    private CandidateMiner candidateMiner;

    @Override
    protected void process(SocketRequest<Block> request) {
        Block data = request.getData();
        long height = data.getHeight();
        String hash = data.getHash();
        String sourceId = request.getSourceId();

        if (blockCacheManager.isContains(hash)) {
            return;
        }

        boolean success = blockService.persistBlockAndIndex(data, sourceId, data.getVersion());
        LOGGER.error("persisted block all info, success={}_height={}_block={}", success, height, hash);

        if (success && !data.isGenesisBlock()) {

            String address = peerManager.getSelf().getId();
            //todo yezaiyong 20180630 inform candidateMiner  cout cminer coin
//            CandidateMiner candidateMiner = new CandidateMiner();
            candidateMiner.instantiationBlock();
            //todo yezaiyong 20180630 infom witiness count time
            if (BlockService.WITNESS_ADDRESS_LIST.contains(address)) {
                WitnessCountTime.instantiationBlock(data);
            }

            Inventory inventory = new Inventory();
            inventory.setHeight(height);
            Set<String> set = new HashSet<>(blockIdxDaoService.getBlockIndexByHeight(height).getBlockHashs());
            inventory.setHashs(set);
            messageCenter.broadcast(new String[]{sourceId}, inventory);
            witnessService.initWitnessTask(blockService.getMaxHeight() + 1);
        }
    }


    /**
     * Steps:
     * 1.Roughly check the witness signatures' count;
     * 2.Check if the block is an orphan block;
     * 3.Thoroughly validate the block;
     * 4.Save the block and block index;
     * 5.Broadcast the persist event;
     * 6.Update the block producer's score;
     * 7.Parse dpos;
     * 8.Chaining the orphan block to the chain;
     */
//    private synchronized boolean processBlock(Block block, String sourceId, short version) {
//        long height = block.getHeight();
//        String blockHash = block.getHash();
//        int sigCount = block.getWitnessSigCount();
//
//        //Check the signature count roughly
//        if (!block.isPreMiningBlock() && sigCount < BlockService.MIN_WITNESS) {
//            LOGGER.warn("The witness number is not enough : sigCount=>{}", sigCount);
//            return false;
//        }
//
//        //Check if orphan block
//        if (blockService.checkOrphanBlock(block, sourceId, version)) {
//            LOGGER.warn("The block is an orphan block: height=>{} hash=>{}", height, blockHash);
//            //If the block was an orphan block always return false.
//            return false;
//        }
//
//        //Valid block thoroughly
//        if (!blockService.validBlock(block)) {
//            LOGGER.error("Validate block failed, height=>{} hash=>{}", height, blockHash);
//            return false;
//        }
//
//        //Save block and index
//        if (!blockService.saveBlockAndIndex(block, blockHash)) {
//            LOGGER.error("Save block and block index failed, height=>{} hash=>{}", height, blockHash);
//            return false;
//        }
//
//        //Broadcast persisted event
//        if (!block.isPreMiningBlock()) {
//            blockService.broadBlockPersistedEvent(block, blockHash);
//        }
//
//        //Do finishing job for the block
//        blockService.finishingJobForBlock(block, sourceId, version);
//
//        return true;
//    }
}
