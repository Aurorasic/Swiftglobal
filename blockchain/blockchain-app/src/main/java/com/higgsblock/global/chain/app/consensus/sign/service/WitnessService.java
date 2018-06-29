package com.higgsblock.global.chain.app.consensus.sign.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.HashBasedTable;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.CandidateBlock;
import com.higgsblock.global.chain.app.blockchain.CandidateBlockHashs;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.consensus.CandidateBlockHandlerTask;
import com.higgsblock.global.chain.app.consensus.NodeManager;
import com.higgsblock.global.chain.app.consensus.vote.Vote;
import com.higgsblock.global.chain.app.consensus.vote.VoteTable;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private NodeManager nodeManager;

    @Autowired
    private BlockService blockService;

    @Autowired
    private CollectWitnessBlockService collectWitnessBlockService;

    @Autowired
    private MessageCenter messageCenter;
    public static final int MAX_SIZE = 5;

    private Cache<Long, List<Block>> sourceBlockMap = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();
    private Cache<Long, List<CandidateBlockHashs>> candidateBlockHashsMap = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();


    @Deprecated
    private CandidateBlockHandlerTask task = null;

    private long height;
    private VoteTable selftVoteTable;
    private Map<String, Block> blockMap;
    private HashBasedTable<Integer, String, Map<String, Vote>> voteTable;
    private Set<String> blockHashAsVersionOne = new HashSet<>();

    private ExecutorService executorService = ExecutorServices.newFixedThreadPool("witnessTask", 1, 5);

    public synchronized void initWitnessTask(long height) {
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
            this.voteTable = HashBasedTable.create(6, 11);
            this.selftVoteTable = new VoteTable(voteTable);
            this.blockMap = new HashMap<>(4);
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

    public synchronized boolean addCandidateBlockFromMiner(Block block) {
        if (block == null) {
            return false;
        }
        if (!block.valid()) {
            LOGGER.info("this block is not valid {}", block);
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

    public synchronized void setBlockHashsFromWitness(CandidateBlockHashs data) {
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

    public synchronized void setBlocksFromWitness(String address, CandidateBlock data) {
        if (task != null && this.height == data.getHeight()) {
            LOGGER.info("the height is {} and the block height is {}", this.height, data.getHeight());
            LOGGER.info("add CandidateBlock to task address {} data {}", address, data);
            task.setBlocksFromWitness(address, data);
        }
    }

    public synchronized void addVoteTable(long voteHeight, HashBasedTable<Integer, String, Map<String, Vote>> voteTable) {
        if (this.height == voteHeight) {
            LOGGER.info("add voteTable to task with voteHeight {} ,voteTable {}", voteHeight, voteTable);
            int i = 1;
            while (true) {
                boolean containsRow = voteTable.containsRow(i);
                if (!containsRow) {
                    break;
                }
                Map<String, Map<String, Vote>> rows = voteTable.row(i);
                Set<Map.Entry<String, Map<String, Vote>>> entrySet = rows.entrySet();
                for (Map.Entry<String, Map<String, Vote>> entry : entrySet) {
                    String pubKey = entry.getKey();
                    Map<String, Vote> voteMapOther = entry.getValue();
                    if (voteMapOther == null || voteMapOther.size() == 0) {
                        continue;
                    }
                    Map<String, Vote> voteMapSelf = this.voteTable.get(i, pubKey);
                    if (voteMapSelf == null) {
                        voteMapSelf = new HashMap<>();
                    }
                    Set<Map.Entry<String, Vote>> voteMapEntrySet = voteMapOther.entrySet();
                    for (Map.Entry<String, Vote> voteMapEntry : voteMapEntrySet) {
                        String blockHash = voteMapEntry.getKey();
                        Vote vote = voteMapEntry.getValue();
                        if (voteMapSelf.containsKey(blockHash)) {
                            //the vote is exist
                            continue;
                        }
                        if (i == 1) {
                            if (blockMap.containsKey(blockHash)) {
                                voteMapSelf.put(blockHash, vote);
                                continue;
                            }
                            //syn blocks
                            continue;
                        }
                        if (blockHashAsVersionOne.contains(blockHash)) {
                            Map<String, Vote> preVoteMap = voteTable.get(i - 1, pubKey);
                            if (preVoteMap == null || preVoteMap.size() != 1) {
                                break;
                            }
                            String proofVersion = vote.getProofVersion();
                            String proofPubKey = vote.getProofPubKey();
                            Map<String, Vote> proofVoteMap = voteTable.get(proofVersion, proofPubKey);

                        }
                    }
                }
                i++;
            }
        } else {
            LOGGER.info("add voteTable to cache with voteHeight {} ,voteTable {}", voteHeight, voteTable);
        }
    }
}
