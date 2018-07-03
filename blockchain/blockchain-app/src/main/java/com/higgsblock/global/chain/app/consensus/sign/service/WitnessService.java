package com.higgsblock.global.chain.app.consensus.sign.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.HashBasedTable;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.CandidateBlock;
import com.higgsblock.global.chain.app.blockchain.CandidateBlockHashs;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.consensus.NodeManager;
import com.higgsblock.global.chain.app.consensus.vote.Vote;
import com.higgsblock.global.chain.app.consensus.vote.VoteTable;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
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
    private Map<String, Block> blockMap;
    private Set<String> blockHashAsVersionOne;
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
                            int voteVersion = vote.getVoteVersion();
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
