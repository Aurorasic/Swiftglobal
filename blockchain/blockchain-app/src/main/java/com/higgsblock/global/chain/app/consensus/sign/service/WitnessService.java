package com.higgsblock.global.chain.app.consensus.sign.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.CandidateBlock;
import com.higgsblock.global.chain.app.blockchain.CandidateBlockHashs;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.consensus.CandidateBlockHandlerTask;
import com.higgsblock.global.chain.app.consensus.NodeManager;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

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

    @Autowired
    private MessageCenter messageCenter;
    public static final int MAX_SIZE = 3;

    private Cache<Long, List<Block>> sourceBlockMap = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();
    private Cache<Long, List<CandidateBlockHashs>> candidateBlockHashsMap = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();


    private volatile CandidateBlockHandlerTask task = null;

    private volatile long height;

    private ExecutorService executorService = ExecutorServices.newFixedThreadPool("witnessTask", 1, 5);

    public void initWitnessTask(long height) {
        if (height <= this.height) {
            return;
        }
        if (task != null && task.getFuture() != null && task.getFuture().cancel(true)) {
            LOGGER.info("cancel the task which height is {}", height);
        }
        String pubKey = keyPair.getPubKey();
        String address = ECKey.pubKey2Base58Address(pubKey);
        if (BlockService.WITNESS_ADDRESS_LIST.contains(address)) {
            LOGGER.info("start the witness task for height {}", height);
            task = new CandidateBlockHandlerTask(keyPair, height, blockService, messageCenter, nodeManager, executorService, collectWitnessBlockService);
            this.height = height;
            List<Block> blocks = sourceBlockMap.getIfPresent(height);
            if (CollectionUtils.isNotEmpty(blocks)) {
                blocks.forEach((block -> addCandidateBlockFromMiner(block)));
            }
            List<CandidateBlockHashs> candidateBlockHashsList = candidateBlockHashsMap.getIfPresent(height);
            if (CollectionUtils.isNotEmpty(candidateBlockHashsList)) {
                task.setBlockHashsListFromWitness(candidateBlockHashsList);
            }
        }
    }

    public boolean addCandidateBlockFromMiner(Block block) {
        if (block == null) {
            return false;
        }
        if (!block.valid()) {
            return false;
        }
        boolean minerPermission = nodeManager.checkProducer(block);
        if (!minerPermission) {
            LOGGER.info("the miner can not package the height block {} {}", block.getHeight(), block.getMinerFirstPKSig().getAddress());
            return false;
        }
        if (task != null && height == block.getHeight()) {
            LOGGER.info("add candidateBlock from miner to task {}", block);
            task.addCandidateBlockFromMiner(block);
        }
        if (height < block.getHeight()) {
            LOGGER.info("add candidateBlock from miner to cache {}", block);
            List<Block> blocks = sourceBlockMap.get(block.getHeight(), (height) -> new LinkedList<>());
            blocks.add(block);
        }
        return true;
    }

    public List<Block> getCandidateBlocksByHeight(long height) {
        if (height == this.height && task != null) {
            return task.getAllCandidateBlocks();
        }
        return new LinkedList<>();
    }

    public List<Block> getCandidateBlocksByHashs(List<String> blockHashs) {
        if (task != null) {
            return task.getCandidateBlocksByHash(blockHashs);
        }
        return new LinkedList<>();
    }

    public List<String> getCandidateBlockHashs(long height) {
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

    public void setBlockHashsFromWitness(CandidateBlockHashs data) {
        if (data == null) {
            return;
        }
        long height = data.getHeight();
        if (task != null && this.height == height) {
            List<String> blockHashs = data.getBlockHashs();
            LOGGER.info("add candidateBlockHashs to task {}", data);
            task.setBlockHashsFromWitness(data.getAddress(), blockHashs);
        }
        if (this.height < height) {
            LOGGER.info("add candidateBlockHashs to cache {}", data);
            List<CandidateBlockHashs> blockHashsList = candidateBlockHashsMap.get(height, (height1) -> new LinkedList<>());
            blockHashsList.add(data);
        }
    }

    public void setBlocksFromWitness(String address, CandidateBlock data) {
        if (task != null && this.height == data.getHeight()) {
            LOGGER.info("the height is {} and the block height is {}", this.height, data.getHeight());
            LOGGER.info("add CandidateBlock to task address {} data {}", address, data);
            task.setBlocksFromWitness(address, data);
        }
    }


}
