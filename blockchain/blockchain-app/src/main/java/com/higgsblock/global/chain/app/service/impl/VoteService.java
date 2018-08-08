package com.higgsblock.global.chain.app.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.SignaturePair;
import com.higgsblock.global.chain.app.blockchain.WitnessTimer;
import com.higgsblock.global.chain.app.blockchain.consensus.message.VoteTable;
import com.higgsblock.global.chain.app.blockchain.consensus.message.VotingBlockRequest;
import com.higgsblock.global.chain.app.blockchain.consensus.vote.Vote;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.service.IVoteService;
import com.higgsblock.global.chain.app.service.IWitnessService;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
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
public class VoteService implements IEventBusListener, IVoteService {

    public static final int MAX_SIZE = 5;

    private static final int MIN_VOTE = 7;

    private static final int MIN_SAME_SIGN = 7;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private IWitnessService witnessService;

    @Autowired
    private IBlockChainService blockChainService;

    @Autowired
    private WitnessTimer witnessTimer;

    private Cache<Long, Map<Integer, Set<Vote>>> voteCache = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();

    @Getter
    private long height;

    /**
     * the row is version of vote,the column is the pubKey of vote,
     * the inner key of Map is the blockHash of the vote
     */
    private VoteTable voteTable = null;

    @Getter
    private Cache<Long, Map<String, Block>> blockCache = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();

    private Block blockWithEnoughSign;

    @Subscribe
    public void process(BlockPersistedEvent event) {
        LOGGER.info("process event: {}", event);
        initWitnessTask(event.getHeight() + 1L);
    }

    public void start() {
        initWitnessTask(blockChainService.getMaxHeight() + 1L);
    }

    @Override
    public synchronized void initWitnessTask(long height) {
        if (height < this.height) {
            return;
        }
        if (witnessService.isWitness(keyPair.getAddress())) {
            if (height == this.height) {
                dealVoteCache();
                return;
            }
            LOGGER.info("start the witness task for height {}", height);
            this.height = height;
            this.voteTable = new VoteTable(new HashMap<>(7), this.height);
            this.blockWithEnoughSign = null;
            Map<String, Block> blockMapCache = this.blockCache.get(height, k -> new HashMap<>(7));
            blockMapCache.values().stream().filter(this::validBlock).findAny().ifPresent(this::voteFirstVote);
            dealVoteCache();
            voteCache.invalidate(height - 3);
            blockCache.invalidate(height - 3);
            LOGGER.info("height {},init witness task success", this.height);
        }
    }

    @Override
    public synchronized void addVotingBlock(Block block) {
        addOriginalBlock(block);
    }

    @Override
    public synchronized void addVotingBlockToCache(Block block) {
        addOriginalBlockToCache(block);
    }

    @Override
    public synchronized void addOriginalBlock(Block block) {
        if (block == null) {
            return;
        }
        if (this.height != block.getHeight() || !validBlock(block)) {
            return;
        }
        this.blockCache.get(block.getHeight(), k -> new HashMap<>(7)).put(block.getHash(), block);
        voteFirstVote(block);
        dealVoteCache();
        messageCenter.dispatchToWitnesses(this.voteTable);
    }

    @Override
    public synchronized void addOriginalBlockToCache(Block block) {
        if (block.getHeight() > height) {
            this.blockCache.get(block.getHeight(), k -> new HashMap<>(7)).put(block.getHash(), block);
        }
    }

    private void voteFirstVote(Block block) {
        if (this.height != block.getHeight()) {
            return;
        }
        Map<String, Vote> voteMap = this.voteTable.getVoteMap(1, keyPair.getPubKey());
        if (MapUtils.isEmpty(voteMap)) {
            String blockHash = block.getHash();
            long blockHeight = block.getHeight();
            LOGGER.info("start vote first vote, height={}, {}", blockHeight, blockHash);
            String bestBlockHash = block.getHash();
            LOGGER.info("add source block from miner and vote height={},{}", this.height, bestBlockHash);
            int voteVersion = 1;
            String proofPubKey = null;
            String proofBlockHash = null;
            int proofVersion = 0;
            Vote vote = createVote(bestBlockHash, block.getHeight(), voteVersion, proofBlockHash, proofPubKey, proofVersion, null);
            this.voteTable.addVote(vote);
            this.messageCenter.dispatchToWitnesses(SerializationUtils.clone(voteTable));
            LOGGER.info("send voteHashTable to witness success height={},{}", this.height, voteTable);
        }
    }

    private void dealVoteCache() {
        LOGGER.info("deal vote cache");
        Map<Integer, Set<Vote>> cache = voteCache.getIfPresent(this.height);
        if (MapUtils.isEmpty(cache)) {
            return;
        }
        int rowSize = cache.size();
        int startAllVoteSize = this.voteTable.getAllVoteSize();
        for (int version = 1; version <= rowSize; version++) {
            if (version > 1 && this.voteTable.getVoteMapOfPubKeyByVersion(version - 1).size() < MIN_VOTE) {
                return;
            }
            int startARowVoteSize = this.voteTable.getARowVoteSize(version);
            Set<Vote> set = cache.get(version);
            Set<Vote> setTemp = new HashSet<>(11);
            Set<Vote> leaderVotes = new HashSet<>(11);
            Set<Vote> followerVotes = new HashSet<>(11);
            set.forEach(vote -> {
                if (isExist(vote)) {
                    return;
                }
                if (vote.isLeaderVote()) {
                    leaderVotes.add(vote);
                } else {
                    followerVotes.add(vote);
                }
            });
            leaderVotes.forEach(vote -> {
                if (vote.getVoteVersion() == 1) {
                    if (!isExist(this.height, vote.getBlockHash())) {
                        Set<String> set1 = new HashSet<>();
                        set1.add(vote.getBlockHash());
                        messageCenter.dispatchToWitnesses(new VotingBlockRequest(set1));
                        setTemp.add(vote);
                        return;
                    }
                }
                if (checkProofAndPreVote(vote)) {
                    this.voteTable.addVote(vote);
                } else {
                    setTemp.add(vote);
                }
            });
            followerVotes.forEach(vote -> {
                if (checkProofAndPreVote(vote)) {
                    this.voteTable.addVote(vote);
                } else {
                    setTemp.add(vote);
                }
            });
            this.voteCache.get(this.height, k -> new HashMap<>(6)).put(version, setTemp);
            if (this.voteTable.getARowVoteSize(version) > startARowVoteSize && collectionVoteSign(version, height)) {
                return;
            }
        }
        if (this.voteTable.getAllVoteSize() > startAllVoteSize) {
            LOGGER.info("local voteHashTable with height={}", height);
            messageCenter.dispatchToWitnesses(SerializationUtils.clone(voteTable));
        }
    }

    @Override
    public synchronized void updateVoteCache(VoteTable otherVoteTable) {
        long voteHeight = otherVoteTable.getHeight();
        if (this.height == voteHeight) {
            return;
        }
        if (this.height > voteHeight) {
            return;
        }
        Map<Integer, Map<String, Map<String, Vote>>> voteMap = otherVoteTable.getVoteTable();
        voteMap.values().forEach(map -> {
            if (MapUtils.isEmpty(map)) {
                return;
            }
            map.values().forEach(map1 -> {
                if (MapUtils.isEmpty(map1)) {
                    return;
                }
                map1.values().forEach(vote -> {
                    if (null == vote) {
                        return;
                    }
                    if (isExist(vote)) {
                        return;
                    }
                    voteCache.get(voteHeight, k -> new HashMap<>(6)).compute(vote.getVoteVersion(), (k, v) -> {
                        if (null == v) {
                            v = new HashSet<>();
                        }
                        v.add(vote);
                        return v;
                    });
                });
            });
        });
    }

    @Override
    public synchronized void dealVoteTable(VoteTable otherVoteTable) {
        long voteHeight = otherVoteTable.getHeight();
        boolean isOver = this.height != voteHeight || blockWithEnoughSign != null;
        if (isOver) {
            LOGGER.info("the voting process of {} is over , current height={}", voteHeight, height);
            return;
        }

        int startAllVoteSize = voteTable.getAllVoteSize();

        LOGGER.info("add voteTable with voteHeight={}:{}", voteHeight, otherVoteTable.toJson());

        int versionSize = otherVoteTable.getVersionSize();
        for (int version = 1; version <= versionSize; version++) {
            Map<String, Map<String, Vote>> newRows = otherVoteTable.getVoteMapOfPubKeyByVersion(version);
            if (MapUtils.isEmpty(newRows)) {
                LOGGER.info("current version has no vote ,version={},voteHeight", version, height);
                return;
            }
            if (version > 1 && otherVoteTable.getVoteMapOfPubKeyByVersion(version - 1).size() < MIN_VOTE) {
                LOGGER.warn("pre version's vote number < {}", MIN_VOTE);
                return;
            }

            int startARowVoteSize = this.voteTable.getARowVoteSize(version);
            Set<Vote> leaderVotes = new HashSet<>(11);
            Set<Vote> followerVotes = new HashSet<>(11);
            newRows.values().forEach(v -> {
                if (MapUtils.isEmpty(v)) {
                    return;
                }
                v.values().forEach(vote -> {
                    if (null == vote || isExist(vote)) {
                        return;
                    }
                    if (vote.isLeaderVote()) {
                        leaderVotes.add(vote);
                    } else {
                        followerVotes.add(vote);
                    }
                });
            });
            leaderVotes.forEach(vote -> {
                if (checkProofAndPreVote(vote)) {
                    this.voteTable.addVote(vote);
                }
            });
            followerVotes.forEach(vote -> {
                if (checkProofAndPreVote(vote)) {
                    this.voteTable.addVote(vote);
                }
            });
            if (this.voteTable.getARowVoteSize(version) > startARowVoteSize && collectionVoteSign(version, this.height)) {
                return;
            }
        }
        if (voteTable.getAllVoteSize() > startAllVoteSize) {
            LOGGER.info("local voteHashTable with height={}", voteHeight);
            messageCenter.dispatchToWitnesses(SerializationUtils.clone(voteTable));
        }
    }

    private boolean collectionVoteSign(int version, long voteHeight) {
        Map<String, Map<String, Vote>> rowVersion = this.voteTable.getVoteMapOfPubKeyByVersion(version);
        if (MapUtils.isEmpty(rowVersion)) {
            LOGGER.info("the vote is empty {},{}", this.height, version);
            return false;
        }
        //row is blockHash,column is pubKey and value is sign
        Table<String, String, String> voteSignTable = HashBasedTable.create();
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
                LOGGER.info("height {},version {},the version is {},set the bestBlockHash to {}", voteHeight, version, bestBlockHash);
            } else {
                if (bestBlockHash.compareTo(voteBlockHash) < 0) {
                    bestBlockHash = voteBlockHash;
                    LOGGER.info("height {},version {},change the bestBlockHash to {}", voteHeight, version, bestBlockHash);
                } else if (bestBlockHash.compareTo(voteBlockHash) == 0) {
                    LOGGER.info("height {},version {},the voteBlockHash is equals the bestBlockHash {}", voteHeight, version, bestBlockHash);
                } else {
                    LOGGER.info("height {},version {},the bestBlockHash do'nt change {},{}", voteHeight, version, bestBlockHash, voteBlockHash);
                }
            }
            voteSignTable.put(voteBlockHash, votePubKey, voteSign);
            Map<String, String> voteRow = voteSignTable.row(voteBlockHash);
            if (voteRow.size() >= MIN_SAME_SIGN) {
                blockWithEnoughSign = this.blockCache.get(this.height, k -> new HashMap<>(7)).get(voteBlockHash);
                if (null == blockWithEnoughSign) {
                    LOGGER.info("the block lost , blockHash : {}", voteBlockHash);
                    return false;
                }
                LOGGER.info("height {},version {},there have enough sign for block {}", voteHeight, version, voteBlockHash);
                List<SignaturePair> signaturePairs = new LinkedList<>();
                Iterator<Map.Entry<String, String>> iterator = voteRow.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> next = iterator.next();
                    SignaturePair signaturePair = new SignaturePair();
                    signaturePair.setPubKey(next.getKey());
                    signaturePair.setSignature(next.getValue());
                    signaturePairs.add(signaturePair);
                }
                blockWithEnoughSign.setVoteVersion(version);
                blockWithEnoughSign.setOtherWitnessSigPairs(signaturePairs);
                LOGGER.info("height {},version {},vote result is {}", voteHeight, version, voteBlockHash);
                messageCenter.dispatchToWitnesses(SerializationUtils.clone(voteTable));
                this.messageCenter.broadcast(blockWithEnoughSign);
                return true;
            }
        }
        LOGGER.info("height {},version {},there haven't enough sign, bestBlockHash is {}", voteHeight, version, bestBlockHash);
        String proofBlockHash = bestBlockHash;
        if (StringUtils.isBlank(bestBlockHash)) {
            LOGGER.info("height {},version {},the bestBlockHash is blank{}", voteHeight, version);
            return false;
        }
        String proofPubKey = voteSignTable.row(bestBlockHash).keySet().iterator().next();
        Map<String, Vote> voteMap = this.voteTable.getVoteMap(version, keyPair.getPubKey());
        if (voteMap == null || voteMap.size() == 0) {
            int proofVersion = version;
            Map<String, Vote> preVoteMap = this.voteTable.getVoteMap(version - 1, keyPair.getPubKey());
            String preBlockHash = preVoteMap.keySet().iterator().next();
            bestBlockHash = bestBlockHash.compareTo(preBlockHash) < 0 ? preBlockHash : bestBlockHash;
            Vote vote = createVote(bestBlockHash, voteHeight, version, proofBlockHash, proofPubKey, proofVersion, preBlockHash);
            LOGGER.info("height {},version {},vote the current version {}", voteHeight, version, vote);
            this.voteTable.addVote(vote);
            return false;
        }
        if (rowVersion.size() < MIN_VOTE) {
            LOGGER.info("there haven't enough vote to check sign {},{},current the number of vote is {}", this.height, version, rowVersion.size());
            return false;
        }
        String currentVersionVoteBlockHash = voteMap.keySet().iterator().next();
        if (StringUtils.equals(currentVersionVoteBlockHash, bestBlockHash)) {
            return false;
        }
        version++;
        voteMap = this.voteTable.getVoteMap(version, keyPair.getPubKey());
        if (voteMap == null || voteMap.size() == 0) {
            int proofVersion = version - 1;
            Vote vote = createVote(bestBlockHash, voteHeight, version, proofBlockHash, proofPubKey, proofVersion, currentVersionVoteBlockHash);
            LOGGER.info("height {},version {},vote the next version {}", voteHeight, version, vote);
            this.voteTable.addVote(vote);
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
        String msg = BlockService.getWitnessSingMessage(vote.getHeight(), vote.getBlockHash(), vote.getVoteVersion());
        String sign = ECKey.signMessage(msg, keyPair.getPriKey());
        vote.setSignature(sign);
        return vote;
    }

    @Override
    public synchronized boolean isExist(long height, String hash) {
        Map<String, Block> map = blockCache.getIfPresent(height);
        return MapUtils.isNotEmpty(map) && map.containsKey(hash);
    }

    private boolean checkProofAndPreVote(Vote vote) {
        if (vote.getVoteVersion() == 1) {
            return true;
        }
        boolean proof = Optional.ofNullable(this.voteTable.getVoteMap(vote.getProofVersion(), vote.getProofPubKey()))
                .map(map -> map.containsKey(vote.getProofBlockHash())).orElse(false);
        boolean pre = Optional.ofNullable(this.voteTable.getVoteMap(vote.getVoteVersion() - 1, vote.getWitnessPubKey()))
                .map(map -> map.containsKey(vote.getPreBlockHash())).orElse(false);
        return proof && pre;
    }

    private boolean isExist(Vote vote) {
        return Optional.ofNullable(this.voteTable.getVoteMap(vote.getVoteVersion(), vote.getWitnessPubKey()))
                .map(map2 -> map2.containsKey(vote.getBlockHash())).orElse(false);
    }

    @Override
    public Block getVotingBlock(String blockHash) {
        return this.blockCache.get(this.height, k -> new HashMap<>(7)).get(blockHash);
    }

    private boolean validBlock(Block block) {
        String pubKey = block.getPubKey();
        String prevBlockHash = block.getPrevBlockHash();
        String blockHash = block.getHash();
        boolean isDposMiner = blockChainService.isDposMiner(ECKey.pubKey2Base58Address(pubKey), prevBlockHash);
        if (!isDposMiner) {
            LOGGER.info("this miner can not package the height, height={}, hash={}", height, blockHash);
            boolean acceptBlock = witnessTimer.checkGuarderPermissionWithTimer(block);
            if (!acceptBlock) {
                LOGGER.error("can not accept this block, height={}, hash={}", height, blockHash);
                return false;
            }
        }
        if (!blockChainService.checkTransactions(block)) {
            LOGGER.error("the transactions are not valid, height={}, hash={}", height, blockHash);
            return false;
        }
        return true;
    }
}
