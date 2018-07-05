package com.higgsblock.global.chain.app.consensus.sign.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.*;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.event.ReceiveOrphanBlockEvent;
import com.higgsblock.global.chain.app.consensus.NodeManager;
import com.higgsblock.global.chain.app.consensus.vote.SourceBlockReq;
import com.higgsblock.global.chain.app.consensus.vote.Vote;
import com.higgsblock.global.chain.app.consensus.vote.VoteTable;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
    private EventBus eventBus;

    @Autowired
    private MessageCenter messageCenter;

    public static final int MAX_SIZE = 5;

    private static final int MIN_VOTE = 8;

    private static final int MIN_SAME_SIGN = 7;

    private Cache<Long, HashBasedTable<Integer, String, Map<String, Vote>>> voteCache = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();

    @Getter
    private long height;

    private HashBasedTable<Integer, String, Map<String, Vote>> voteTable;

    @Getter
    private Map<String, Block> blockMap;

    @Autowired
    private BlockService blockService;

    private Block blockWithEnoughSign;

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
            this.blockWithEnoughSign = null;
            this.blockMap = new HashMap<>();
            HashBasedTable<Integer, String, Map<String, Vote>> voteTableInCache = voteCache.getIfPresent(height);
            if (voteTableInCache != null) {
                dealVoteTable(null, this.height, voteTableInCache);
            }
            LOGGER.info("height {},init witness task success", this.height);
        }
    }

    public synchronized void addSourceBlock(Block block) {
        if (block == null) {
            return;
        }
        if (this.height > block.getHeight()) {
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
            boolean isWitnessTimer = WitnessCountTime.isCurrBlockConfirm(block);
            LOGGER.info("verify witness timer block is sure {} block hash {}", isWitnessTimer, block.getHash());
            if (!isWitnessTimer) {
                LOGGER.info("verify witness timer block is accept {} ", isWitnessTimer);
                return;
            }
            return;
        }
        boolean valid = blockService.validBlockFromProducer(block);
        if (!valid) {
            LOGGER.info("the block is not valid {} {}", block.getHeight(), blockHash);
        }
        if (this.height == block.getHeight()) {
            if (this.blockMap == null) {
                this.blockMap = new HashMap<>();
            }
            this.blockMap.put(block.getHash(), block);
            Map<String, Vote> voteMap = this.voteTable.get(1, keyPair.getPubKey());
            if (voteMap != null && voteMap.size() > 0) {
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
            String proofBlockHash = null;
            int proofVersion = 0;
            Vote vote = createVote(bestBlockHash, block.getHeight(), voteVersion, proofBlockHash, proofPubKey, proofVersion, null);
            voteMap.put(bestBlockHash, vote);
            this.voteTable.put(1, keyPair.getPubKey(), voteMap);
            Map<Integer, Map<String, Map<String, Vote>>> integerMapMap = this.voteTable.rowMap();
            VoteTable voteTable = new VoteTable(integerMapMap);
            this.messageCenter.dispatchToWitnesses(voteTable);
            LOGGER.info("send voteTable to witness success {},{}", this.height, voteTable);
            return;
        }
    }

    private void updateVoteCache(long height, HashBasedTable<Integer, String, Map<String, Vote>> voteTable) {
        HashBasedTable<Integer, String, Map<String, Vote>> oldCacheVote = voteCache.get(height, k -> voteTable);
        if (voteTable.rowKeySet().size() < oldCacheVote.rowKeySet().size()) {
            return;
        } else if (voteTable.rowKeySet().size() == oldCacheVote.rowKeySet().size()
                && voteTable.size() < oldCacheVote.size()) {
            return;
        }
        voteCache.put(height, voteTable);
    }

    @Deprecated
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

    @Deprecated
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
        if (version == 1) {
            localVoteMap.put(blockHash, vote);
        } else {
            Map<String, Vote> proofVoteMap = this.voteTable.get(vote.getProofVersion(), vote.getProofPubKey());
            String proofBlockHash = vote.getProofBlockHash();
            if (proofVoteMap == null || !proofVoteMap.containsKey(proofBlockHash)) {
                return false;
            }

            Map<String, Vote> preVoteMap = this.voteTable.get(version - 1, pubKey);
            String preBlockHash = vote.getPreBlockHash();
            if (preVoteMap == null || !preVoteMap.containsKey(preBlockHash)) {
                return false;
            }

            //follower's vote
            if (vote.getProofVersion() == version) {
                boolean isValid = false;
                if (blockHash.compareTo(proofBlockHash) == 0 && proofBlockHash.compareTo(preBlockHash) >= 0) {
                    isValid = true;
                } else if (blockHash.compareTo(preBlockHash) == 0 && preBlockHash.compareTo(proofBlockHash) >= 0) {
                    isValid = true;
                }
                if (!isValid) {
                    LOGGER.info("follower's vote is illegal vote : {}", vote);
                    return false;
                }
            }
            //leader's vote
            else if (blockHash.compareTo(proofBlockHash) != 0 || blockHash.compareTo(preBlockHash) <= 0) {
                LOGGER.info("leader's vote is illegal vote : {}", vote);
                return false;
            }

            localVoteMap.put(blockHash, vote);
        }
        this.voteTable.put(version, pubKey, localVoteMap);
        return true;
    }


    public synchronized void dealVoteTable(String sourceId, long voteHeight, HashBasedTable<Integer, String, Map<String, Vote>> voteTable) {


        boolean isOver = this.height > voteHeight || (this.height == voteHeight && blockWithEnoughSign != null);
        if (isOver) {
            LOGGER.info("the voting process of {} is over , current height : {}", voteHeight, height);
            return;
        }
        if (voteHeight > this.height) {
            updateVoteCache(voteHeight, voteTable);
            eventBus.post(new ReceiveOrphanBlockEvent(height, null, sourceId));
            LOGGER.info("the height is greater than local , add voteTable to cache");
            return;
        }

        if (null != voteTable.row(1)) {
            Set<String> blockHashs = new HashSet<>();
            voteTable.row(1).values().forEach(map -> {
                map.forEach((k, v) -> {
                    if (!blockMap.containsKey(k)) {
                        blockHashs.add(k);
                    }
                });
            });
            if (blockHashs.size() > 0) {
                messageCenter.unicast(sourceId, new SourceBlockReq(blockHashs));
                updateVoteCache(voteHeight, voteTable);
                LOGGER.info("source blocks is not enough,add vote table to cache");
                return;
            }
        }

        LOGGER.info("add voteTable to task with voteHeight {} ,voteTable {}", voteHeight, voteTable);
        int startAllVoteSize = getAllVoteSize();
        int rowSize = voteTable.rowKeySet().size();
        for (int version = 1; version <= rowSize; version++) {
            if (!voteTable.containsRow(version)) {
                return;
            }
            Map<String, Map<String, Vote>> newRows = voteTable.row(version);
            if (newRows.size() == 0) {
                return;
            }
            if (version > 1 && voteTable.row(version - 1).size() < MIN_VOTE) {
                LOGGER.warn("pre version's vote number < {}", MIN_VOTE);
                return;
            }
            int startARowVoteSize = getARowVoteSize(version);
            Map<String, Map<String, Vote>> leaderVotes = new HashMap<>(6);
            Map<String, Map<String, Vote>> followerVotes = new HashMap<>(6);
            for (Map.Entry<String, Map<String, Vote>> entry : newRows.entrySet()) {
                String pubKey = entry.getKey();
                if (StringUtils.isBlank(pubKey)) {
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
                        leaderVotes.compute(pubKey, (k, v) ->
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
                LOGGER.info("leaders' votes are illegal votes : {}", leaderVotes);
                return;
            }
            if (!verifyVotes(followerVotes)) {
                LOGGER.info("followers' votes are illegal votes : {}", leaderVotes);
                return;
            }
            if (getARowVoteSize(version) > startARowVoteSize && collectionVoteSign(version, voteHeight)) {
                return;
            }
        }
        if (getAllVoteSize() > startAllVoteSize) {
            messageCenter.dispatchToWitnesses(new VoteTable(this.voteTable.rowMap()));
        }
    }

    private int getAllVoteSize() {
        int size = 0;
        for (Map<String, Map<String, Vote>> row : this.voteTable.rowMap().values()) {
            for (Map<String, Vote> map : row.values()) {
                size += map.size();
            }
        }
        return size;
    }

    private int getARowVoteSize(int version) {
        int size = 0;
        for (Map<String, Vote> map : this.voteTable.row(version).values()) {
            size += map.size();
        }
        return size;
    }


    private boolean collectionVoteSign(int version, long voteHeight) {
        Map<String, Map<String, Vote>> rowVersion = this.voteTable.row(version);
        if (rowVersion.size() < MIN_VOTE) {
            LOGGER.info("there haven't enough vote to check sign {},current the number of vote is {}", this.height, rowVersion.size());
            return false;
        }
        //row is blockHash,column is pubKey and value is sign
        Table<String, String, String> VoteSignTable = HashBasedTable.create();
        Set<Map.Entry<String, Map<String, Vote>>> voteEntrySet = rowVersion.entrySet();
        LOGGER.info("the version is {},the voteHeight is {} and the votes are {}", version, voteHeight, voteEntrySet);
        String bestBlockHash = null;
        for (Map.Entry<String, Map<String, Vote>> voteEntry : voteEntrySet) {
            Map<String, Vote> voteEntryValue = voteEntry.getValue();
            String votePubKey = voteEntry.getKey();
            if (voteEntryValue == null || voteEntryValue.size() != 1) {
                LOGGER.info("height {},version {},the {} has {} vote", voteHeight, votePubKey, version, voteEntryValue == null ? 0 : voteEntryValue.size());
                continue;
            }
            Vote vote = voteEntryValue.values().iterator().next();
            if (vote == null) {
                continue;
            }
            String voteBlockHash = vote.getBlockHash();
            String voteSign = vote.getSignature();
            if (StringUtils.isBlank(bestBlockHash)) {
                bestBlockHash = voteBlockHash;
                LOGGER.info("height {},version {} ,set the bestBlockHash to {}", voteHeight, version, bestBlockHash);
            } else {
                bestBlockHash = bestBlockHash.compareTo(voteBlockHash) < 0 ? voteBlockHash : bestBlockHash;
                LOGGER.info("height {},version {},change the bestBlockHash to {}", voteHeight, version, bestBlockHash);
            }
            VoteSignTable.put(voteBlockHash, votePubKey, voteSign);
            Map<String, String> voteRow = VoteSignTable.row(voteBlockHash);
            if (voteRow.size() >= MIN_SAME_SIGN) {
                blockWithEnoughSign = blockMap.get(voteBlockHash);
                LOGGER.info("height {},version {},there have enough sign for block {}", voteHeight, version, voteBlockHash);
                List<BlockWitness> blockWitnesses = new LinkedList<>();
                Iterator<Map.Entry<String, String>> iterator = voteRow.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> next = iterator.next();
                    BlockWitness blockWitness = new BlockWitness();
                    blockWitness.setPubKey(next.getKey());
                    blockWitness.setSignature(next.getValue());
                    blockWitnesses.add(blockWitness);
                }
                blockWithEnoughSign.setVoteVersion(version);
                blockWithEnoughSign.setOtherWitnessSigPKS(blockWitnesses);
                LOGGER.info("height {},version {},vote result is {}", voteHeight, version, blockWithEnoughSign);
                messageCenter.dispatchToWitnesses(new VoteTable(this.voteTable.rowMap()));
                this.messageCenter.broadcast(blockWithEnoughSign);
                return true;
            }
        }
        LOGGER.info("height {},version {},there haven't enough sign, bestBlockHash is {}", voteHeight, version, bestBlockHash);
        String proofBlockHash = bestBlockHash;
        String proofPubKey = VoteSignTable.row(bestBlockHash).keySet().iterator().next();
        Map<String, Vote> voteMap = this.voteTable.get(version, keyPair.getPubKey());
        if (voteMap == null || voteMap.size() == 0) {
            int proofVersion = version;
            Map<String, Vote> preVoteMap = this.voteTable.get(version - 1, keyPair.getPubKey());
            String preBlockHash = preVoteMap.keySet().iterator().next();
            Vote vote = createVote(bestBlockHash, voteHeight, version, proofBlockHash, proofPubKey, proofVersion, preBlockHash);
            LOGGER.info("height {},version {},vote the current version {}", voteHeight, version, vote);
            voteMap = new HashMap<>();
            voteMap.put(bestBlockHash, vote);
            this.voteTable.put(version, keyPair.getPubKey(), voteMap);
            return false;
        }
        String currentVersionVoteBlockHash = voteMap.keySet().iterator().next();
        if (StringUtils.equals(currentVersionVoteBlockHash, bestBlockHash)) {
            return false;
        }
        version++;
        voteMap = this.voteTable.get(version, keyPair.getPubKey());
        if (voteMap == null || voteMap.size() == 0) {
            int proofVersion = version - 1;
            Vote vote = createVote(bestBlockHash, voteHeight, version, proofBlockHash, proofPubKey, proofVersion, currentVersionVoteBlockHash);
            LOGGER.info("height {},version {},vote the next version {}", voteHeight, version, vote);
            voteMap = new HashMap<>();
            voteMap.put(bestBlockHash, vote);
            this.voteTable.put(version, keyPair.getPubKey(), voteMap);
            return false;
        }
        return false;
    }

    private Vote createVote(String bestBlockHash, long voteHeight, int version, String proofBlockHash, String proofPubKey, int proofVersion, String preBlockHash) {
        Vote vote = new Vote();
        vote.setBlockHash(bestBlockHash);
        vote.setHeight(voteHeight);
        vote.setVoteVersion(version);
        vote.setWitnessPubKey(keyPair.getPubKey());
        vote.setProofPubKey(proofPubKey);
        vote.setProofVersion(proofVersion);
        vote.setProofBlockHash(proofBlockHash);
        vote.setPreBlockHash(preBlockHash);
        String msg = blockService.getWitnessSingMessage(vote.getHeight(), vote.getBlockHash(), vote.getVoteVersion());
        String sign = ECKey.signMessage(msg, keyPair.getPriKey());
        vote.setSignature(sign);
        return vote;
    }

    private boolean validVoteSignature(Vote vote) {
        if (vote == null) {
            return false;
        }
        return blockService.validSign(vote.getHeight(), vote.getBlockHash(), vote.getVoteVersion(), vote.getSignature(), vote.getWitnessPubKey());
    }
}
