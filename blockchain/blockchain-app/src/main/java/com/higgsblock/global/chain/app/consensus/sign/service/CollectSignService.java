package com.higgsblock.global.chain.app.consensus.sign.service;

import com.alibaba.fastjson.JSON;
import com.higgsblock.global.chain.app.Application;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.BlockWitness;
import com.higgsblock.global.chain.app.consensus.SignBlockScoreStrategy;
import com.higgsblock.global.chain.app.consensus.sign.model.WitnessSign;
import com.higgsblock.global.chain.app.service.BlockReqService;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;


/**
 * @author yangyi
 * @date 2018/3/6
 */
@Component
@Slf4j
public class CollectSignService {

    public final static int witnessNum = 3;
    public final static int WITNESSBATCHNUM = 12;
    public static final int RETRY_REQ_MAX_NUM = 200;
    private static final int MIN_TASK_SIZE = 3;
    public static final int MIN_MISECOND = 30 * 1000;
    @Autowired
    private KeyPair peerKeyPair;
    @Autowired
    private BlockService blockService;
    @Resource(name = "witnessedBlock")
    private ConcurrentMap<Long, Block> witnessedBlock;
    @Autowired
    private BlockReqService blockReqService;

    @Autowired
    private PeerManager peerManager;

    private ExecutorService executorService = ExecutorServices.newFixedThreadPool("collectSign", 3, 100);

    public Set<Integer> getValidateSignHeightGap(long height) {
        Set<Integer> result = new HashSet<>();
        Map<String, Map<String, Long>> prevRoundWitness = findPrevRoundWitness(height);
        if (prevRoundWitness != null || prevRoundWitness.size() > 0) {
            Iterator<Map<String, Long>> iterator = prevRoundWitness.values().iterator();
            while (iterator.hasNext()) {
                Map<String, Long> next = iterator.next();
                Long signHeight = next.values().iterator().next();
                if (signHeight == null) {
                    continue;
                }
                result.add((int) (height - signHeight));
            }
        }
        LOGGER.info("for height={}, validate sign height gap set is: {}", height, result);
        return result;
    }

    public Map<String, Map<String, Long>> findPrevRoundWitness(long height) {
        Map<String, Map<String, Long>> result = new HashMap<>();
        int count = WITNESSBATCHNUM;
        long startHeight = height;
        height--;
        while (count > 0) {
            Block block = blockService.getBestBlockByHeight(height);
            if (block == null) {
                throw new RuntimeException("the best block at " + height + " is empty when calculate the preRoundWitness " + startHeight);
            }
            BlockWitness minerPKSig = block.getMinerFirstPKSig();
            String pubKey = minerPKSig.getPubKey();
            if (StringUtils.isBlank(pubKey)) {
                throw new RuntimeException("the best block at " + height + " is empty when calculate the preRoundWitness " + startHeight);
            }
            String address = ECKey.pubKey2Base58Address(pubKey);
            if (result.containsKey(address)) {
                height--;
                continue;
            }
            Map<String, Long> map = new HashMap<>();
            map.put(block.getHash(), height);
            result.put(address, map);
            count--;
            height--;
            if (startHeight - height > SignBlockScoreStrategy.MAX_HIGH_GAP) {
                break;
            }
        }
        return result;
    }

    public Block getWitnessed(Long height) {
        return witnessedBlock.get(height);
    }

    public Block addWitnessed(Long height, Block block) {
        return witnessedBlock.put(height, block);
    }

    /**
     * Witness create sign for the creator.
     */
    public WitnessSign createSign(Block block) {
        String sig = ECKey.signMessage(block.getHash(), peerKeyPair.getPriKey());
        WitnessSign sign = new WitnessSign(block.getHash(), sig, peerKeyPair.getPubKey());
        LOGGER.info("Create witness sign : {}", JSON.toJSONString(sign));
        return sign;
    }

    /**
     * Creator sends the signed block to other witnesses for resigning.
     */
    public void sendBlockToWitness(Block block) {
        LOGGER.info("pack new block {}", block);
        long height = block.getHeight();
        if (height > Application.PRE_BLOCK_COUNT) {
            List<Peer> peers = peerManager.getByIds(BlockService.WITNESS_ADDRESS_LIST.toArray(new String[BlockService.WITNESS_ADDRESS_LIST.size()]));
            ConcurrentHashMap<String, Callable> taskMap = new ConcurrentHashMap<>(11);
            for (Peer peer : peers) {
                Callable task = () -> {
                    try {
                        if (peer == null) {
                            return null;
                        }
                        Boolean tryAgain = blockReqService.sendBlockToWitness(peer.getIp(), peer.getHttpServerPort(), block);
                        if (!tryAgain) {
                            taskMap.remove(peer.getId());
                        }
                    } catch (Throwable e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                };
                taskMap.put(peer.getId(), task);
            }
            try {
                LOGGER.info("begin to send block to witness");
                trySendBlockToWitness(taskMap, height);
            } catch (InterruptedException e) {
                LOGGER.info(e.getMessage(), e);
            }
        }
    }

    private void trySendBlockToWitness(Map<String, Callable> taskMap, long height) throws InterruptedException {
        int retryNum = 0;
        while (retryNum++ < RETRY_REQ_MAX_NUM && taskMap.size() >= MIN_TASK_SIZE) {
            long bestMaxHeight = blockService.getBestMaxHeight();
            if (bestMaxHeight != height - 1) {
                break;
            }
            Collection values = taskMap.values();
            Collection tasks = new LinkedList<>();
            tasks.addAll(values);
            long timeMillis = System.currentTimeMillis();
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
    }

}
