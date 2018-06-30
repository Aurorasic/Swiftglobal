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
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
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
    private Block blockWithEnoughSign = null;

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
            if (blockWithEnoughSign != null) {
                return;
            }
            LOGGER.info("add voteTable to task with voteHeight {} ,voteTable {}", voteHeight, voteTable);
            int version = 1;
            while (true) {
                boolean containsRow = voteTable.containsRow(version);
                if (!containsRow) {
                    break;
                }
                boolean addVoteToLocal = false;
                Map<String, Map<String, Vote>> rows = voteTable.row(version);
                Set<Map.Entry<String, Map<String, Vote>>> entrySet = rows.entrySet();
                for (Map.Entry<String, Map<String, Vote>> entry : entrySet) {
                    String pubKey = entry.getKey();
                    Map<String, Vote> voteMapOther = entry.getValue();
                    if (voteMapOther == null || voteMapOther.size() == 0) {
                        continue;
                    }
                    Map<String, Vote> voteMapForVersion = this.voteTable.get(version, pubKey);
                    if (voteMapForVersion == null) {
                        voteMapForVersion = new HashMap<>();
                    }
                    Set<Map.Entry<String, Vote>> voteMapEntrySet = voteMapOther.entrySet();
                    for (Map.Entry<String, Vote> voteMapEntry : voteMapEntrySet) {
                        String blockHash = voteMapEntry.getKey();
                        Vote vote = voteMapEntry.getValue();
                        if (voteMapForVersion.containsKey(blockHash)) {
                            //the vote is exist
                            continue;
                        }
                        if (!validVoteSign(vote)) {
                            continue;
                        }
                        if (version == 1) {
                            if (blockMap.containsKey(blockHash)) {
                                voteMapForVersion.put(blockHash, vote);
                                addVoteToLocal = true;
                                continue;
                            }
                            //syn blocks
                            continue;
                        }
                        if (blockHashAsVersionOne.contains(blockHash)) {
                            //todo yangyi the version maybe is not consecutive
                            Map<String, Vote> preVoteMap = this.voteTable.get(version - 1, pubKey);
                            if (preVoteMap == null || preVoteMap.size() != 1) {
                                break;
                            }
                            Vote preVote = preVoteMap.values().iterator().next();
                            String preBlockHash = preVote.getBlockHash();

                            int proofVersion = vote.getProofVersion();
                            String proofPubKey = vote.getProofPubKey();
                            String voteBlockHash = vote.getBlockHash();
                            int voteVersion = vote.getVersion();
                            Map<String, Vote> proofVoteMap = voteTable.get(proofVersion, proofPubKey);
                            if (proofVoteMap == null || proofVoteMap.size() != 1) {
                                continue;
                            }
                            Vote proofVote = proofVoteMap.values().iterator().next();
                            if (!validVoteSign(proofVote)) {
                                continue;
                            }
                            String proofBlockHash = proofVote.getBlockHash();
                            boolean validProofResult = validProof(proofVersion, proofBlockHash, preBlockHash, voteVersion, voteBlockHash);
                            if (validProofResult) {
                                voteMapForVersion.put(blockHash, vote);
                                addVoteToLocal = true;
                            }
                        }
                    }
                    if (addVoteToLocal && voteMapForVersion.size() > 0) {
                        this.voteTable.put(version, pubKey, voteMapForVersion);
                    }
                }
                if (!addVoteToLocal) {
                    continue;
                }
                collectionVoteSign(version, voteHeight);
                if (blockWithEnoughSign == null) {
                    version++;
                    continue;
                }
                //broadcast block with 7 sign
                messageCenter.broadcast(blockWithEnoughSign);
                break;
            }
        } else {
            LOGGER.info("add voteTable to cache with voteHeight {} ,voteTable {}", voteHeight, voteTable);
        }
    }

    private void collectionVoteSign(int version, long voteHeight) {
        Map<String, Map<String, Vote>> rowVersion = this.voteTable.row(version);
        Map<String, List<String>> voteSignMap = new HashMap<>();
        Set<Map.Entry<String, Map<String, Vote>>> voteEntrySet = rowVersion.entrySet();
        String bestBlockHash = null;
        String proofPubKey = null;
        int validVoteCount = 0;
        for (Map.Entry<String, Map<String, Vote>> voteEntry : voteEntrySet) {
            Map<String, Vote> voteEntryValue = voteEntry.getValue();
            if (voteEntryValue == null || voteEntryValue.size() != 1) {
                continue;
            }
            String votePubKey = voteEntry.getKey();
            String voteBlockHash = voteEntryValue.keySet().iterator().next();
            if (StringUtils.isBlank(bestBlockHash)) {
                bestBlockHash = voteBlockHash;
            } else {
                bestBlockHash = bestBlockHash.compareTo(voteBlockHash) < 0 ? voteBlockHash : bestBlockHash;
                proofPubKey = votePubKey;
            }
            List<String> pubKeyList = voteSignMap.computeIfAbsent(votePubKey, (key) -> new ArrayList<>());
            pubKeyList.add(votePubKey);
            validVoteCount++;
            if (pubKeyList.size() >= 7) {
                blockWithEnoughSign = blockMap.get(voteBlockHash);
            }
        }
        if (blockWithEnoughSign != null) {
            return;
        }
        if (validVoteCount < 7) {
            return;
        }
        Map<String, Vote> voteMap = this.voteTable.get(version + 1, keyPair.getPubKey());
        if (voteMap == null || voteMap.size() == 0) {
            Vote vote = new Vote();
            vote.setBlockHash(bestBlockHash);
            vote.setHeight(voteHeight);
            vote.setVersion(version + 1);
            vote.setWitnessPubKey(keyPair.getPubKey());
            vote.setProofPubKey(proofPubKey);
            vote.setProofVersion(version);
            String msg = getSingMessage(vote);
            String sign = ECKey.signMessage(msg, keyPair.getPriKey());
            vote.setSignature(sign);
            voteMap = new HashMap<>();
            voteMap.put(bestBlockHash, vote);
            this.voteTable.put(version + 1, keyPair.getPubKey(), voteMap);
            messageCenter.broadcast(SerializationUtils.clone(this.selftVoteTable));
        }
    }

    private String getSingMessage(Vote vote) {
        return vote.getHeight() + vote.getBlockHash() + vote.getVersion();
    }

    private boolean validProof(int proofVersion, String proofBlockHash, String preBlockHash, int voteVersion, String voteBlockHash) {
        //todo yangyi
        return true;
    }

    private boolean validVoteSign(Vote vote) {
        if (vote == null) {
            return false;
        }
        String msg = getSingMessage(vote);
        return ECKey.verifySign(msg, vote.getSignature(), vote.getWitnessPubKey());
    }
}
