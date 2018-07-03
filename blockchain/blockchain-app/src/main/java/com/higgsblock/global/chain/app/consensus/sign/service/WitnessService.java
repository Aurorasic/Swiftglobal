package com.higgsblock.global.chain.app.consensus.sign.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.HashBasedTable;
import com.higgsblock.global.chain.app.blockchain.*;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.consensus.NodeManager;
import com.higgsblock.global.chain.app.consensus.vote.Vote;
import com.higgsblock.global.chain.app.consensus.vote.VoteTable;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private MessageCenter messageCenter;

    public static final int MAX_SIZE = 5;

    private Cache<Long, Block> betterSourceBlockCache = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();
    private Cache<Long, Map<String, Block>> sourceBlockCache = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();
    private Cache<Long, List<CandidateBlockHashs>> sourceBlockHashCache = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();

    private long height;

    private HashBasedTable<Integer, String, Map<String, Vote>> voteTable;

    private VoteTable selftVoteTable;

    @Getter
    private Map<String, Block> blockMap;

    private Set<String> blockHashAsVersionOne;

    private Block blockWithEnoughSign;

    @Getter
    private List<HashBasedTable<Integer, String, Map<String, Vote>>> voteCache = new ArrayList<>();

    public synchronized void initWitnessTask(long height) {
        if (height <= this.height) {
            return;
        }
        String pubKey = keyPair.getPubKey();
        String address = ECKey.pubKey2Base58Address(pubKey);
        if (BlockService.WITNESS_ADDRESS_LIST.contains(address)) {
            LOGGER.info("start the witness task for height {}", height);
            this.height = height;
            this.voteTable = HashBasedTable.create(6, 11);
            this.selftVoteTable = new VoteTable(voteTable);
            this.blockWithEnoughSign = null;
            this.blockMap = null;
            this.blockHashAsVersionOne = new HashSet<>();
            Block block = betterSourceBlockCache.getIfPresent(height);
            addSourceBlock(block);
            this.blockMap.putAll(sourceBlockCache.get(this.height, (tempHeight) -> new HashMap<>()));
            List<CandidateBlockHashs> candidateBlockHashsList = sourceBlockHashCache.get(this.height, tempHeight -> new LinkedList<>());
            candidateBlockHashsList.forEach(candidateBlockHashs -> setBlockHashsFromWitness(candidateBlockHashs));
            //todo yangyi process voteTable in cache
        }
    }

    public synchronized void addSourceBlock(Block block) {
        if (block == null) {
            return;
        }
        String blockHash = block.getHash();
        if (!block.valid()) {
            LOGGER.info("this block is not valid,height {}, {}", block.getHeight(), blockHash);
            return;
        }
        if (blockMap != null && blockMap.containsKey(block.getHash())) {
            LOGGER.info("this block is exist in blockMap,height{},{}", block.getHeight(), blockHash);
            return;
        }
        boolean minerPermission = nodeManager.checkProducer(block);
        if (!minerPermission) {
            LOGGER.info("the miner can not package the height block {} {}", block.getHeight(), blockHash);
            return;
        }
        if (this.height > block.getHeight()) {
            return;
        }
        if (this.height == block.getHeight()) {
            if (this.blockMap == null) {
                this.blockMap = new HashMap<>();
            }
            this.blockMap.put(block.getHash(), block);
            Map<String, Vote> voteMap = this.voteTable.get(1, keyPair.getPubKey());
            if (voteMap != null && voteMap.size() == 1) {
                LOGGER.info("the vote of version one is exist {},{}", this.height, blockHash);
                return;
            }
            if (voteMap == null) {
                voteMap = new HashMap<>();
            }
            LOGGER.info("add source block from miner and vote {},{}", this.height, blockHash);
            String bestBlockHash = block.getHash();
            int voteVersion = 1;
            String proofPubKey = null;
            Vote vote = createVote(bestBlockHash, block.getHeight(), voteVersion, proofPubKey);
            voteMap.put(bestBlockHash, vote);
            this.voteTable.put(1, keyPair.getPubKey(), voteMap);
            Object clone = SerializationUtils.clone(selftVoteTable);
            this.messageCenter.dispatchToWitnesses(clone);
            LOGGER.info("send voteTable to witness success {},{}", this.height, clone);
            Set<String> blockHashs = this.blockMap.keySet();
            CandidateBlockHashs candidateBlockHashs = new CandidateBlockHashs();
            candidateBlockHashs.setHeight(this.height);
            candidateBlockHashs.setBlockHashs(new LinkedList<>(blockHashs));
            candidateBlockHashs.setPubKey(keyPair.getPubKey());
            candidateBlockHashs.setAddress(ECKey.pubKey2Base58Address(keyPair.getPubKey()));
            candidateBlockHashs.setSignature(ECKey.signMessage(candidateBlockHashs.getHash(), keyPair.getPriKey()));
            this.messageCenter.dispatchToWitnesses(candidateBlockHashs);
            LOGGER.info("send candidateBlockHashList to witness success {},{}", this.height, candidateBlockHashs);
            return;
        }
        //todo yezaiyong 20180630 add witnessTimer
        boolean isWitnessTimer = WitnessCountTime.isCurrBlockConfirm(block);
        LOGGER.info("verify witness timer block is sure {} block hash {}",isWitnessTimer,block.getHash());
        if (!isWitnessTimer){
            LOGGER.info("verify witness timer block is accept {} ",isWitnessTimer);
            return;
        }

        Map<String, Block> blockMap = sourceBlockCache.get(block.getHeight(), (tempHeight) -> new HashMap<>());
        if (blockMap.containsKey(blockHash)) {
            return;
        }
        blockMap.put(block.getHash(), block);
        LOGGER.info("add source block to cache {},{}", this.height, blockHash);
        Block oldBlock = betterSourceBlockCache.getIfPresent(block.getHeight());
        if (oldBlock == null) {
            betterSourceBlockCache.put(block.getHeight(), block);
            LOGGER.info("set blockHash into cache {},{},{}", this.height, blockHash);
            return;
        }
        String oldBlockHash = oldBlock.getHash();
        boolean isBetter = blockHash.compareTo(oldBlockHash) > 0;
        if (isBetter) {
            betterSourceBlockCache.put(block.getHeight(), block);
            LOGGER.info("change blockHash in cache {},{},{}", this.height, blockHash, oldBlock);
        } else {
            LOGGER.info("the oldBlockHash in cache is better than new blockHash  {},{},{}", this.height, blockHash, oldBlock);
        }
        return;
    }

    public synchronized void setBlocksFromWitness(CandidateBlock data) {
        long height = data.getHeight();
        if (this.height > height) {
            LOGGER.info("set blocks from witness late {} ,{},{}", height, this.height, data);
            return;
        }
        LOGGER.info("set blocks from witness {} ,{}", height, data);
        List<Block> blocks = data.getBlocks();
        if (blocks == null || blocks.size() == 0) {
            blocks.forEach(block -> addSourceBlock(block));
        }
    }

    public synchronized void setBlockHashsFromWitness(CandidateBlockHashs data) {
        if (data == null) {
            return;
        }
        long height = data.getHeight();
        if (this.height > height) {
            return;
        }
        if (this.height == height) {
            List<String> blockHashs = data.getBlockHashs();
            LOGGER.info("add candidateBlockHashs height {},{}", this.height, blockHashs);
            if (CollectionUtils.isEmpty(blockHashs)) {
                return;
            }
            HashSet<String> blockHashSet = new HashSet<>(blockHashs);
            List<Block> moreBlockList = new LinkedList<>();
            Set<Map.Entry<String, Block>> entrySet = this.blockMap.entrySet();
            entrySet.forEach((entry) -> {
                String key = entry.getKey();
                if (!blockHashSet.contains(key)) {
                    moreBlockList.add(entry.getValue());
                }
            });
            if (moreBlockList.size() > 0) {
                CandidateBlock candidateBlock = new CandidateBlock();
                candidateBlock.setPubKey(keyPair.getPubKey());
                candidateBlock.setBlocks(moreBlockList);
                candidateBlock.setHeight(this.height);
                candidateBlock.setSignature(ECKey.signMessage(candidateBlock.getHash(), keyPair.getPriKey()));
                this.messageCenter.unicast(data.getAddress(), candidateBlock);
                LOGGER.info("unicast candidateBlock to {} {}", data.getAddress(), this.height);
            }
        }
        if (this.height < height) {
            LOGGER.info("add candidateBlockHashs to cache {}", data);
            List<CandidateBlockHashs> blockHashsList = sourceBlockHashCache.get(height, (height1) -> new LinkedList<>());
            blockHashsList.add(data);
        }
    }


    private boolean verifyVotes(Map<String, Map<String, Vote>> votes) {
        for (Map.Entry<String, Map<String, Vote>> entry : votes.entrySet()) {
            if (entry.getValue().size() > 0) {
                for (Map.Entry<String, Vote> voteMapEntry : entry.getValue().entrySet()) {
                    Vote value = voteMapEntry.getValue();
                    if (!dealSingleVote(value)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * verify and add to local map
     *
     * @param vote
     * @return
     */
    private boolean dealSingleVote(Vote vote) {
        int version = vote.getVoteVersion();
        String pubKey = vote.getWitnessPubKey();
        String blockHash = vote.getBlockHash();
        Map<String, Vote> localVoteMap = this.voteTable.get(version, pubKey);
        if (localVoteMap == null) {
            localVoteMap = new HashMap<>(2);
        }
        if (localVoteMap.containsKey(blockHash)) {
            //the vote is exist
            return true;
        }
        if (!validVoteSignature(vote)) {
            return false;
        }
        if (version == 1 && blockMap.containsKey(blockHash)) {
            localVoteMap.put(blockHash, vote);
        } else {
            Map<String, Vote> proofVoteMap = this.voteTable.get(vote.getProofVersion(), vote.getProofPubKey());
            if (proofVoteMap == null) {
                return false;
            }
            Vote proofVote = proofVoteMap.get(vote.getProofBlockHash());
            if (null == proofVote) {
                return false;
            }
            String proofBlockHash = proofVote.getBlockHash();

            Map<String, Vote> preVoteMap = this.voteTable.get(version - 1, pubKey);
            if (preVoteMap == null || !preVoteMap.containsKey(vote.getPreBlockHash())) {
                return false;
            }
            String preBlockHash = proofVote.getPreBlockHash();


            //follower's vote
            if (vote.getProofVersion() == version) {
                boolean isValid = false;
                if (blockHash.compareTo(proofBlockHash) == 0 && proofBlockHash.compareTo(preBlockHash) >= 0) {
                    isValid = true;
                } else if (blockHash.compareTo(preBlockHash) == 0 && preBlockHash.compareTo(proofBlockHash) >= 0) {
                    isValid = true;
                }
                if (!isValid) {
                    return false;
                }
            }
            //leader's vote
            else if (blockHash.compareTo(proofBlockHash) != 0 || blockHash.compareTo(preBlockHash) <= 0) {
                return false;
            }

            localVoteMap.put(blockHash, vote);
        }
        this.voteTable.put(version, pubKey, localVoteMap);
        return true;
    }


    public synchronized void dealVoteTable(long voteHeight, HashBasedTable<Integer, String, Map<String, Vote>> voteTable) {

        boolean isOver = this.height > voteHeight || (this.height == voteHeight && blockWithEnoughSign != null);
        if (isOver) {
            LOGGER.info("the voting process of {} is over", height);
            return;
        }

        if (this.height < voteHeight) {
            // TODO: 7/3/2018 yuanjiantao add voteTable to cache
            LOGGER.info("add voteTable to cache with voteHeight {} ,voteTable {}", voteHeight, voteTable);
            return;
        }

        LOGGER.info("add voteTable to task with voteHeight {} ,voteTable {}", voteHeight, voteTable);
        int rowSize = voteTable.rowKeySet().size();
        for (int version = 1; version <= rowSize; version++) {
            if (!voteTable.containsRow(version)) {
                return;
            }
            Map<String, Map<String, Vote>> newRows = voteTable.row(version);
            if (newRows.size() == 0) {
                return;
            }
            int startVoteSize = this.voteTable.size();
            Map<String, Map<String, Vote>> leaderVotes = new HashMap<>(6);
            Map<String, Map<String, Vote>> followerVotes = new HashMap<>(6);
            for (Map.Entry<String, Map<String, Vote>> entry : newRows.entrySet()) {
                String pubKey = entry.getKey();
                if (null == pubKey) {
                    continue;
                }
                Map<String, Vote> newVoteMap = entry.getValue();
                if (newVoteMap == null || newVoteMap.isEmpty()) {
                    continue;
                }
                for (Map.Entry<String, Vote> voteMapEntry : newVoteMap.entrySet()) {
                    String blockHash = voteMapEntry.getKey();
                    Vote vote = voteMapEntry.getValue();
                    if (null == blockHash || null == vote || null == vote.getBlockHash()) {
                        continue;
                    }
                    if (version != vote.getVoteVersion()
                            || !pubKey.equals(vote.getWitnessPubKey())
                            || !blockHash.equals(vote.getBlockHash())
                            || vote.getHeight() != this.height
                            || null == vote.getSignature()) {
                        return;
                    }
                    if (version == 1) {
                        followerVotes.compute(pubKey, (k, v) ->
                                null == v ? new HashMap<>(3) : v
                        ).put(vote.getBlockHash(), vote);
                    } else if (vote.getProofVersion() == version) {
                        followerVotes.compute(pubKey, (k, v) ->
                                null == v ? new HashMap<>(3) : v
                        ).put(vote.getBlockHash(), vote);
                    } else if (vote.getProofVersion() + 1 == version) {
                        leaderVotes.compute(pubKey, (k, v) ->
                                null == v ? new HashMap<>(3) : v
                        ).put(vote.getBlockHash(), vote);
                    }
                }
            }
            if (leaderVotes.size() == 0) {
                return;
            }
            if (!verifyVotes(leaderVotes)) {
                return;
            }
            if (followerVotes.size() == 0 || !verifyVotes(followerVotes)) {
                return;
            }
            int endVoteSize = this.voteTable.size();
            if (endVoteSize > startVoteSize) {
                collectionVoteSign(version, voteHeight);
                if (blockWithEnoughSign == null) {
                    version++;
                    continue;
                }
                //broadcast block with 7 sign
                messageCenter.broadcast(blockWithEnoughSign);
            }

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
            Vote vote = createVote(bestBlockHash, voteHeight, version, proofPubKey);
            voteMap = new HashMap<>();
            voteMap.put(bestBlockHash, vote);
            this.voteTable.put(version + 1, keyPair.getPubKey(), voteMap);
            messageCenter.broadcast(SerializationUtils.clone(this.selftVoteTable));
        }
    }

    private Vote createVote(String bestBlockHash, long voteHeight, int version, String proofPubKey) {
        Vote vote = new Vote();
        vote.setBlockHash(bestBlockHash);
        vote.setHeight(voteHeight);
        vote.setVoteVersion(version + 1);
        vote.setWitnessPubKey(keyPair.getPubKey());
        vote.setProofPubKey(proofPubKey);
        vote.setProofVersion(version);
        String msg = getSingMessage(vote);
        String sign = ECKey.signMessage(msg, keyPair.getPriKey());
        vote.setSignature(sign);
        return vote;
    }

    private String getSingMessage(Vote vote) {
        return vote.getHeight() + vote.getBlockHash() + vote.getVoteVersion();
    }


    private boolean validVoteSignature(Vote vote) {
        if (vote == null) {
            return false;
        }
        String msg = getSingMessage(vote);
        return ECKey.verifySign(msg, vote.getSignature(), vote.getWitnessPubKey());
    }
}
