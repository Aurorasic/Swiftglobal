package com.higgsblock.global.chain.app.consensus;

import com.google.common.collect.HashBasedTable;
import com.higgsblock.global.chain.app.blockchain.*;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.consensus.sign.service.CollectWitnessBlockService;
import com.higgsblock.global.chain.app.consensus.vote.Vote;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author yangyi
 * @deta 2018/4/26
 * @description
 */
public class CandidateBlockHandlerTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Map<String, Block> candidateBlocksFromMiner = new ConcurrentHashMap<>();
    private ArrayList<Block> candidateAllBlocks = new ArrayList(5);
    private CountDownLatch countDownLatch = null;
    public static final int MAX_GAP_BLOCK_NUM = 2;

    private volatile boolean flag = false;
    private Semaphore semaphore = new Semaphore(10);
    private KeyPair keyPair;
    private String address;

    private Block recommendBlock = null;
    private List<String> minerAddresses = null;
    private ExecutorService executorService = null;
    private CollectWitnessBlockService collectWitnessBlockService;
    private BlockService blockService;
    private MessageCenter messageCenter;

    private long height;
    private Future future;
    private int fullBlockCount;
    private boolean firstMiner = false;
    private Block firstMinerBlock = null;
    private String sourceBlockWitnessAddress = null;
    private List<CandidateBlockHashs> candidateBlockHashsList = new ArrayList<>();
    private List<String> relasedAddress = new ArrayList<>(10);

    public CandidateBlockHandlerTask(KeyPair keyPair, long height, BlockService blockService, MessageCenter messageCenter, NodeManager nodeManager, ExecutorService executorService, CollectWitnessBlockService collectWitnessBlockService) {
        this.height = height;
        this.keyPair = keyPair;
        address = ECKey.pubKey2Base58Address(keyPair);
        minerAddresses = nodeManager.getDposGroupByHeihgt(this.height);
        this.executorService = executorService;
        this.blockService = blockService;
        this.collectWitnessBlockService = collectWitnessBlockService;
        this.messageCenter = messageCenter;
        fullBlockCount = nodeManager.getFullBlockCountByHeight(height);
        countDownLatch = new CountDownLatch(fullBlockCount);
    }

    private boolean validCandidateBlock(Block block) {
        if (block == null) {
            logger.error("the candidate block is null");
            return false;
        }
        if (!block.valid()) {
            logger.warn("the candidate block is not valid {}", block);
            return false;
        }
        return blockService.validBlockFromProducer(block);
    }

    public Future addCandidateBlockFromMiner(Block block) {
        if (!validCandidateBlock(block)) {
            logger.warn("the candidate block from miner is not valid {}", block);
            return future;
        }
        synchronized (this) {
            if (candidateBlocksFromMiner.size() == 0) {
                logger.info("receive the first candidate block from miner with the height {}", block.getHeight());
            }
            boolean firstMiner = isTheFirstMiner(block);
            this.firstMiner = firstMiner ? firstMiner : this.firstMiner;
            candidateBlocksFromMiner.computeIfAbsent(block.getHash(), s -> {
                logger.info("get new candidate block with hash {}", s);
                countDownLatch.countDown();
                if (firstMiner) {
                    firstMinerBlock = block;
                    long count = countDownLatch.getCount();
                    for (long i = 0; i < count; i++) {
                        countDownLatch.countDown();
                    }
                }
                return block;
            });
            boolean submit = candidateBlocksFromMiner.size() == (fullBlockCount - MAX_GAP_BLOCK_NUM) || firstMiner;
            logger.info("the fullBlockCount is {},the size of candidateBlocksFromMiner is {}", fullBlockCount, candidateBlocksFromMiner.size());
            if (submit && future == null) {
                try {
                    future = executorService.submit(this);
                    logger.info("submit the task success {}", this.height);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                    logger.info("submit the task fail {}", this.height);
                }
            }
        }
        return future;
    }

    private boolean isTheFirstMiner(Block block) {
        BlockWitness minerFirstPKSig = block.getMinerFirstPKSig();
        if (minerFirstPKSig == null || CollectionUtils.isEmpty(minerAddresses)) {
            return false;
        }
        return StringUtils.equals(minerFirstPKSig.getAddress(), minerAddresses.get(0));
    }

    private Block selectRecommendBlock(List<Block> blocks) {
        int minerIndex = -1;
        int blockIndex = -1;
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            String pubKey = block.getPubKey();
            String minerAddress = ECKey.pubKey2Base58Address(pubKey);
            int index = minerAddresses.indexOf(minerAddress);
            if (index == -1) {
                logger.error("can not find this address in the miner address");
                continue;
            }
            if (minerIndex == -1 || minerIndex > index) {
                minerIndex = index;
                blockIndex = i;
            } else if (minerIndex == index) {
                //todo yangyi repeat miner
            }
        }
        logger.info("the blockIndex is {} and minerAddress is {} and the blocks is {}", minerIndex, minerAddresses, blocks);
        if (blockIndex >= 0 && blockIndex < blocks.size()) {
            Block block = blocks.get(blockIndex);
            Block clone = SerializationUtils.clone(block);
            if (clone != null) {
                String sig = ECKey.signMessage(clone.getHash(), keyPair.getPriKey());
                clone.addWitnessSignature(keyPair.getPubKey(), sig, null);
                return clone;
            }
        }
        return null;
    }


    @Override
    public void run() {
        logger.info("begin to collection candidate block from other witness");
        try {
            if (!this.firstMiner) {
                logger.info("wait for candidate block with height {}", this.height);
                countDownLatch.await(10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            return;
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        synchronized (this) {
            flag = true;
        }
        process();
        logger.info("witness success {}", height);
    }

    public void process() {
        try {
            recommendBlock = null;
            Collection<Block> values = candidateBlocksFromMiner.values();
            values.forEach(block -> {
                if (!candidateAllBlocks.contains(block)) {
                    candidateAllBlocks.add(block);
                }
            });
            semaphore.acquire(10);
            CandidateBlockHashs candidateBlockHashs = new CandidateBlockHashs();
            candidateBlockHashs.setBlockHashs(getCandidateBlockHashs());
            candidateBlockHashs.setHeight(this.height);
            candidateBlockHashs.setAddress(this.address);
            candidateBlockHashs.setPubKey(keyPair.getPubKey());
            candidateBlockHashs.setSignature(ECKey.signMessage(candidateBlockHashs.getHash(), keyPair.getPriKey()));
            logger.info("send myself block hash to other witness {}", candidateBlockHashs);
            messageCenter.dispatchToWitnesses(candidateBlockHashs);
            if (CollectionUtils.isNotEmpty(candidateBlockHashsList)) {
                candidateBlockHashsList.forEach(hashs -> {
                    String address = hashs.getAddress();
                    List<String> blockHashs = hashs.getBlockHashs();
                    setBlockHashsFromWitness(address, blockHashs);
                });
            }
            logger.info("wait 8 release");
            try {
                semaphore.acquire(8);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
                return;
            }
            logger.info("wait 8 release success");

            recommendBlock = selectRecommendBlock(candidateAllBlocks);
            logger.info("select the recommend block success {}", recommendBlock);
            collectWitnessBlockService.reInit(height);
            collectWitnessBlockService.processBlockAfterCollected(SerializationUtils.clone(recommendBlock));
            collectWitnessBlockService.collectAllSignedBlockAndBroadcast(recommendBlock);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setBlockHashsFromWitness(String address, List<String> blockHashsFromWitness) {
        if (blockHashsFromWitness == null) {
            blockHashsFromWitness = new LinkedList<>();
        }
        if (!flag) {
            synchronized (this) {
                if (!flag) {
                    CandidateBlockHashs candidateBlockHashs = new CandidateBlockHashs();
                    candidateBlockHashs.setBlockHashs(blockHashsFromWitness);
                    candidateBlockHashs.setHeight(this.height);
                    candidateBlockHashs.setAddress(address);
                    logger.info("add candidateBlockHashs to cache {}", candidateBlockHashs);
                    candidateBlockHashsList.add(candidateBlockHashs);
                    if (future != null || StringUtils.isNotBlank(sourceBlockWitnessAddress)) {
                        return;
                    }
                    candidateBlockHashs = new CandidateBlockHashs();
                    candidateBlockHashs.setBlockHashs(new LinkedList<>());
                    candidateBlockHashs.setHeight(this.height);
                    candidateBlockHashs.setAddress(this.address);
                    candidateBlockHashs.setAddress(ECKey.pubKey2Base58Address(keyPair.getPubKey()));
                    candidateBlockHashs.setPubKey(keyPair.getPubKey());
                    candidateBlockHashs.setSignature(ECKey.signMessage(candidateBlockHashs.getHash(), keyPair.getPriKey()));
                    messageCenter.unicast(address, candidateBlockHashs);
                    this.sourceBlockWitnessAddress = address;
                }
            }
        }
        logger.info("receive the blockHash from {} with blockHash {}", address, blockHashsFromWitness);
        if (firstMinerBlock != null) {
            logger.info(" firstMiner block exist {}", firstMiner);
            if (!blockHashsFromWitness.contains(firstMinerBlock.getHash())) {
                CandidateBlock candidateBlock = new CandidateBlock();
                candidateBlock.setHeight(this.height);
                List<Block> blocks = new ArrayList<>();
                blocks.add(firstMinerBlock);
                candidateBlock.setBlocks(blocks);
                candidateBlock.setPubKey(keyPair.getPubKey());
                String signMessage = ECKey.signMessage(candidateBlock.getHash(), keyPair.getPriKey());
                candidateBlock.setSignature(signMessage);
                logger.info(" send firstMiner block to {} ", address);
                messageCenter.unicast(address, candidateBlock);
            }
            semaphore.release();
            relasedAddress.add(address);
            logger.info(" release address {} ", address);
            return;
        }
        List<String> candidateBlockHashs = getCandidateBlockHashs();
        List<String> moreBlockHash = calcAMore2B(candidateBlockHashs, blockHashsFromWitness);
        List<Block> candidateBlocksByHash = getCandidateBlocksByHash(moreBlockHash);
        if (CollectionUtils.isNotEmpty(candidateBlocksByHash)) {
            CandidateBlock candidateBlock = new CandidateBlock();
            candidateBlock.setHeight(this.height);
            List<Block> blocks = new ArrayList<>();
            blocks.addAll(candidateBlocksByHash);
            candidateBlock.setBlocks(blocks);
            candidateBlock.setPubKey(keyPair.getPubKey());
            String signMessage = ECKey.signMessage(candidateBlock.getHash(), keyPair.getPriKey());
            candidateBlock.setSignature(signMessage);
            logger.info(" send more block to {} and block with hashs {}", address, moreBlockHash);
            messageCenter.unicast(address, candidateBlock);
        }
        moreBlockHash = calcAMore2B(blockHashsFromWitness, candidateBlockHashs);
        if (CollectionUtils.isEmpty(moreBlockHash)) {
            logger.info(" has all block hash from {} ", address);
            semaphore.release();
            relasedAddress.add(address);
            logger.info(" release address {} ", address);
        }
    }

    public void setBlockHashsListFromWitness(List<CandidateBlockHashs> candidateBlockHashsList) {
        this.candidateBlockHashsList.addAll(candidateBlockHashsList);
    }

    public void setBlocksFromWitness(String address, CandidateBlock data) {
        List<Block> blocksFromWitness = data.getBlocks();
        logger.info(" get more block from {} with block ", address, blocksFromWitness);
        addCandidateBlocksFromWitness(blocksFromWitness);
        if (!relasedAddress.contains(address)) {
            semaphore.release();
            relasedAddress.add(address);
            logger.info(" release address {} ", address);
        }
        if (StringUtils.equals(sourceBlockWitnessAddress, address) && future == null) {
            synchronized (this) {
                if (future == null) {
                    future = executorService.submit(this);
                }
            }
        }
    }

    private List<String> calcAMore2B(final List<String> listA, final List<String> listB) {
        List diffList = new LinkedList();
        if (CollectionUtils.isEmpty(listA)) {
            return diffList;
        }

        if (CollectionUtils.isEmpty(listB)) {
            return listA;
        }
        for (String a : listA) {
            if (!listB.contains(a)) {
                diffList.add(a);
            }
        }
        return diffList;
    }

    public synchronized int addCandidateBlocksFromWitness(Collection<Block> blocks) {
        int count = 0;
        for (Block block : blocks) {
            if (block != null && block.getHeight() == height && validCandidateBlock(block)) {
                if (!candidateAllBlocks.contains(block)) {
                    boolean firstMiner = isTheFirstMiner(block);
                    this.firstMiner = firstMiner ? firstMiner : this.firstMiner;
                    firstMinerBlock = block;
                    candidateAllBlocks.add(block);
                    count++;
                }
            }
        }
        return count;
    }

    public List<Block> getAllCandidateBlocks() {
        if (flag) {
            return candidateAllBlocks;
        } else {
            return new LinkedList();
        }
    }

    public List<Block> getCandidateBlocksByHash(List<String> blockHashs) {
        List<Block> result = new LinkedList<>();
        if (flag && CollectionUtils.isNotEmpty(blockHashs)) {
            for (Block block : candidateAllBlocks) {
                if (blockHashs.contains(block.getHash())) {
                    result.add(block);
                }
            }
        }
        return result;
    }

    public List<String> getCandidateBlockHashs() {
        List<String> result = new LinkedList<>();
        if (flag) {
            if (firstMinerBlock != null) {
                result.add(firstMinerBlock.getHash());
                return result;
            }
            for (Block block : candidateAllBlocks) {
                result.add(block.getHash());
            }
        }

        return result;
    }

    public Block getRecommendBlock() {
        return recommendBlock;
    }

    public Future getFuture() {
        return future;
    }

    public void addVoteTable(HashBasedTable<Integer, String, Vote> voteTable) {
    }
}
