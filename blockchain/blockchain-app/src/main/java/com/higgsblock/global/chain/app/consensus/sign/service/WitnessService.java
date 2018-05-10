package com.higgsblock.global.chain.app.consensus.sign.service;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.consensus.CandidateBlockHandlerTask;
import com.higgsblock.global.chain.app.consensus.NodeManager;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author yangyi
 * @deta 2018/4/26
 * @description
 */
@Service
@Slf4j
public class WitnessService {

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private PeerManager peerManager;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private BlockService blockService;

    @Autowired
    private CollectWitnessBlockService collectWitnessBlockService;

    private volatile CandidateBlockHandlerTask task = null;

    private Future future;

    private volatile long height;

    private ExecutorService executorService = ExecutorServices.newFixedThreadPool("witnessTask", 3, 50);

    public void initWitnessTask(long height) {
        if (height <= this.height) {
            return;
        }
        if (future != null && future.cancel(true)) {
            LOGGER.info("cancel the task which height is {}", height);
        }
        future = null;
        task = null;
        String pubKey = keyPair.getPubKey();
        String address = ECKey.pubKey2Base58Address(pubKey);
        if (BlockService.WITNESS_ADDRESS_LIST.contains(address)) {
            task = new CandidateBlockHandlerTask(keyPair, height, blockService, nodeManager, peerManager, executorService, collectWitnessBlockService);
            this.height = height;
        }
    }

    public boolean addCandidateBlockFromMiner(Block block) {
        if (block == null) {
            return false;
        }
        if (task != null && height == block.getHeight()) {
            future = task.addCandidateBlockFromMiner(block);
            return false;
        }
        if (height > block.getHeight()) {
            return false;
        }
        return true;
    }

    public Collection<Block> getCandidateBlocksByHeight(long height) {
        if (height == this.height && task != null) {
            return task.getCandidateAllBlocks();
        }
        return new LinkedList<>();
    }

    public Collection<Block> getCandidateBlocksByHashs(Collection<String> blockHashs) {
        if (task != null) {
            return task.getCandidateBlocksByHash(blockHashs);
        }
        return new LinkedList<>();
    }

    public Collection<String> getCandidateBlockHashs(long height) {
        if (height == this.height && task != null) {
            return task.getCandidateBlockHashs();
        }
        return new LinkedList<>();
    }

    public int addCandidateBlocksFromWitness(Collection<Block> blocks) {
        if (task != null) {
            return task.addCandidateBlocksFromWitness(blocks);
        }
        return 0;
    }

    public Block getRecommendBlock(long height) {
        if (task != null) {
            Block recommendBlock = task.getRecommendBlock();
            if (recommendBlock != null && recommendBlock.getHeight() == height) {
                return recommendBlock;
            }
        }
        return null;
    }


}
