package com.higgsblock.global.chain.app.consensus;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.BlockWitness;
import com.higgsblock.global.chain.app.consensus.sign.service.CollectWitnessBlockService;
import com.higgsblock.global.chain.app.service.IWitnessApi;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import com.higgsblock.global.chain.network.http.HttpClient;
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
    private PeerManager peerManager;
    private Map<String, Block> candidateBlocksFromMiner = new ConcurrentHashMap<>();
    private ArrayList<Block> candidateAllBlocks = new ArrayList(5);
    private CountDownLatch countDownLatch = null;
    public static final int RETRY_REQ_MAX_NUM = 200;
    public static final int MAX_GAP_BLOCK_NUM = 2;

    private static final int MIN_TASK_SIZE = 2;
    public static final int MIN_MISECOND = 3 * 1000;
    private volatile boolean flag = false;
    private KeyPair keyPair;
    private String address;

    private Block recommendBlock = null;
    private List<String> minerAddresses = null;
    private List<String> witnessAddresses = BlockService.WITNESS_ADDRESS_LIST;
    private ExecutorService executorService = null;
    private CollectWitnessBlockService collectWitnessBlockService;
    private BlockService blockService;

    private long height;
    private Future future;
    private int fullBlockCount;

    public CandidateBlockHandlerTask(KeyPair keyPair, long height, BlockService blockService, NodeManager nodeManager, PeerManager peerManager, ExecutorService executorService, CollectWitnessBlockService collectWitnessBlockService) {
        this.height = height;
        this.keyPair = keyPair;
        address = ECKey.pubKey2Base58Address(keyPair);
        minerAddresses = nodeManager.getDposGroup(this.height);
        this.peerManager = peerManager;
        this.executorService = executorService;
        this.blockService = blockService;
        this.collectWitnessBlockService = collectWitnessBlockService;
        fullBlockCount = nodeManager.getFullBlockCountByHeight(height);
        countDownLatch = new CountDownLatch(fullBlockCount);
    }

    public Future addCandidateBlockFromMiner(Block block) {
        if (block == null) {
            logger.error("the candidate block is null");
            return future;
        }
        if (!block.valid()) {
            logger.warn("the candidate block is not valid {}", block);
            return future;
        }
        boolean fromProducer = blockService.validBlockFromProducer(block);
        if (!fromProducer) {
            logger.warn("the candidate block from miner is not valid {}", block);
            return future;
        }
        synchronized (this) {
            if (candidateBlocksFromMiner.size() == 0) {
                logger.info("receive the first candidate block from miner with the height {}", block.getHeight());
            }
            boolean firstMiner = isTheFirstMiner(block);
            candidateBlocksFromMiner.computeIfAbsent(block.getHash(), s -> {
                logger.info("get new candidate block with hash {}", s);
                countDownLatch.countDown();
                if (firstMiner) {
                    long count = countDownLatch.getCount();
                    for (long i = 0; i < count; i++) {
                        countDownLatch.countDown();
                    }
                }
                return block;
            });
            boolean submit = future == null && (candidateBlocksFromMiner.size() == (fullBlockCount - MAX_GAP_BLOCK_NUM) || firstMiner);
            if (submit) {
                future = executorService.submit(this);
            }
        }
        return future;
    }

    private boolean isTheFirstMiner(Block block) {
        BlockWitness minerFirstPKSig = block.getMinerFirstPKSig();
        if (minerFirstPKSig != null || CollectionUtils.isEmpty(minerAddresses)) {
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
        try {
            logger.info("wait for candidate block with height {}", this.height);
            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        flag = true;
        process();
        logger.info("witness success {}", height);
    }

    public void process() {
        try {
            recommendBlock = null;
            candidateAllBlocks.clear();

            candidateAllBlocks.addAll(candidateBlocksFromMiner.values());
            Map<String, Callable> taskMap = new ConcurrentHashMap<>(10);
            witnessAddresses.forEach(address -> {
                if (StringUtils.equals(address, this.address)) {
                    return;
                }
                Peer peer = peerManager.getById(address);
                if (peer == null) {
                    return;
                }
                String ip = peer.getIp();
                int port = peer.getHttpServerPort();
                //todo yangyi move to service
                IWitnessApi api = HttpClient.getApi(ip, port, IWitnessApi.class);
                Callable task = () -> {
                    try {
                        logger.info("begin to swap candidate blocks with witness={}", address);

                        //step 1: fetch block hashs from other witness
                        Collection<String> fromWitnessBlockHashs = api.getCandidateBlockHashs(height).execute().body();
                        if (CollectionUtils.isEmpty(fromWitnessBlockHashs)) {
                            logger.info("fromWitnessBlockHashs is empty,try next. witness={}", address);
                            return null;
                        }
                        Collection<String> myCandidateBlockHashs = getCandidateBlockHashs();
                        Collection<String> otherWitnessDiffBlockHashs = calcAMore2B(fromWitnessBlockHashs, myCandidateBlockHashs);
                        Collection<String> myDiffBlockHashs = calcAMore2B(myCandidateBlockHashs, fromWitnessBlockHashs);
                        logger.info("begin my candidate blocks={},the witness={} candidate blocks={}", myCandidateBlockHashs, address, fromWitnessBlockHashs);

                        //step 2: get different blocks that other witness more than mine
                        if (CollectionUtils.isNotEmpty(otherWitnessDiffBlockHashs)) {
                            Collection<Block> otherWinessDiffBlocks = api.getCandidateBlocksByHashs(otherWitnessDiffBlockHashs).execute().body();
                            //if other witness have selected besb block and stoped this height witness, he will return empty blocks
                            if (CollectionUtils.isNotEmpty(otherWinessDiffBlocks)) {
                                addCandidateBlocksFromWitness(otherWinessDiffBlocks);
                            }
                        }

                        //step 3: put different blocks to other witness that mine more than other witness
                        if (CollectionUtils.isNotEmpty(myDiffBlockHashs)) {
                            Collection<Block> myDiffBlocks = getCandidateBlocksByHash(myDiffBlockHashs);
                            api.putBlocksToWitness(myDiffBlocks).execute().body();
                        }
                        logger.info("end my candidate blocks={}", myCandidateBlockHashs);

                        taskMap.remove(address);
                    } catch (Exception e) {
                        logger.error("error for swap blocks with witness=" + address, e);
                    }
                    return null;
                };
                taskMap.put(address, task);
            });
            try {
                logger.info("begin to get query other witness's candidate block with height {}", height);
                collectCandidateBlocks(taskMap);
                logger.info("query other witness's candidate block success with height {}", height);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            recommendBlock = selectRecommendBlock(candidateAllBlocks);
            logger.info("select the recommend block success {}", recommendBlock);
            collectWitnessBlockService.reInit(height, executorService);
            collectWitnessBlockService.addBlock(SerializationUtils.clone(recommendBlock));
            boolean success = collectWitnessBlockService.collectAllSignedBlockAndBroadcast();
            if (!success && blockService.getBestMaxHeight() < height) {
                logger.error("collect sign fail,try again,the height is {}", height);
                process();
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Collection<String> calcAMore2B(final Collection<String> A, final Collection<String> B) {
        List diffList = new LinkedList();
        if (CollectionUtils.isEmpty(A)) {
            return diffList;
        }

        if (CollectionUtils.isEmpty(B)) {
            return A;
        }
        for (String a : A) {
            if (!B.contains(a)) {
                diffList.add(a);
            }
        }
        return diffList;
    }

    public synchronized int addCandidateBlocksFromWitness(Collection<Block> blocks) {
        int count = 0;
        for (Block block : blocks) {
            if (block != null && block.getHeight() == height) {
                if (!block.valid()) {
                    logger.error("the candidate block is not valid");
                }
                if (!blockService.validBlockCommon(block)) {
                    logger.error("the candidate block from witness is not valid");
                }
                if (!candidateAllBlocks.contains(block)) {
                    candidateAllBlocks.add(block);
                    count++;
                }
            }
        }
        return count;
    }


    private void collectCandidateBlocks(Map<String, Callable> taskMap) throws InterruptedException {
        int retryNum = 0;
        while (retryNum++ < RETRY_REQ_MAX_NUM && taskMap.size() >= MIN_TASK_SIZE) {
            Collection values = taskMap.values();
            Collection tasks = new LinkedList<>();
            tasks.addAll(values);
            long timeMillis = System.currentTimeMillis();
            logger.info("try again with retryNum {} and task size {}", retryNum, taskMap.size());
            List<Future> futures = executorService.invokeAll(tasks, 5, TimeUnit.SECONDS);
            futures.forEach(future -> {
                if (!future.isDone()) {
                    future.cancel(false);
                }
            });
            timeMillis = MIN_MISECOND - (System.currentTimeMillis() - timeMillis);
            if (timeMillis > 0) {
                Thread.sleep(timeMillis);
            }
        }
        retryNum = 0;
        while (retryNum++ < 3 && taskMap.size() != 0) {
            Collection values = taskMap.values();
            Collection tasks = new LinkedList<>();
            tasks.addAll(values);
            logger.info("try again with retryNum {} and task size {}", retryNum, taskMap.size());
            executorService.invokeAll(tasks, 5, TimeUnit.SECONDS);
            Thread.sleep(1000);
        }
    }

    public Collection<Block> getCandidateAllBlocks() {
        if (flag) {
            return candidateAllBlocks;
        } else {
            return new LinkedList();
        }
    }

    public Collection<Block> getCandidateBlocksByHash(Collection<String> blockHashs) {
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

    public Collection<String> getCandidateBlockHashs() {
        List<String> result = new LinkedList<>();
        if (flag) {
            for (Block block : candidateAllBlocks) {
                result.add(block.getHash());
            }
        }

        return result;
    }

    public Block getRecommendBlock() {
        return recommendBlock;
    }
}
