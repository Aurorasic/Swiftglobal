package cn.primeledger.cas.global.consensus.sign;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.consensus.SignBlockScoreStrategy;
import cn.primeledger.cas.global.consensus.sign.model.WitnessSign;
import cn.primeledger.cas.global.consensus.sign.service.CollectSignService;
import cn.primeledger.cas.global.crypto.ECKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author yangyi
 * @deta 2018/3/14
 * @description
 */
@Service
@Slf4j
public class WitnessManager {

    @Autowired
    private BlockService blockService;

    private ConcurrentHashMap<Long, WitnessSign> witnessMap = new ConcurrentHashMap<Long, WitnessSign>();

    private CountDownLatch countDownLatch;

    private Map<String, Long> prevRoundWitnessMap;

    private Block block;

    private volatile boolean init = false;

    public void init(CountDownLatch countDownLatch, Map<String, Long> prevRoundWitnessMap, Block block) {
        this.countDownLatch = countDownLatch;
        this.prevRoundWitnessMap = prevRoundWitnessMap;
        this.block = block;
        init = true;
    }

    public void close() {
        this.countDownLatch = null;
        this.prevRoundWitnessMap = null;
        this.witnessMap.clear();
        this.block = null;
        init = false;
    }

    public void receiveWitness(WitnessSign data) {
        long blockHeight = block.getHeight();
        LOGGER.info("receive witness {} for block with height", data, blockHeight);
        if (!init || countDownLatch == null || prevRoundWitnessMap == null || block == null) {
            return;
        }
        String pubKey = data.getPubKey();
        String address = ECKey.pubKey2Base58Address(pubKey);
        Long height = prevRoundWitnessMap.get(address);
        if (height == null) {
            LOGGER.error("receive witness {} but the signer is not right {}", data, blockHeight);
            return;
        }
        LOGGER.info("the signer's height is {} for blockHeight {}", height, blockHeight);
        boolean valid = blockService.validateWitness(data.getBlockHash(), pubKey, data.getSignature(), block);
        if (!valid) {
            LOGGER.info("receive witness but the sign is not right from blockHash", block.getHash());
            return;
        }
        if (witnessMap.size() < CollectSignService.witnessNum) {
            witnessMap.put(height, data);
            LOGGER.info("add witness with height {}", height);
        } else {
            Long lastHeight = height;
            Enumeration<Long> keys = witnessMap.keys();
            while (keys.hasMoreElements()) {
                Long aLong = keys.nextElement();
                if (lastHeight > aLong) {
                    lastHeight = aLong;
                }
            }
            if (lastHeight.compareTo(height) != 0) {
                witnessMap.remove(lastHeight);
                LOGGER.info("remove witness with height {}", lastHeight);
                witnessMap.put(height, data);
                LOGGER.info("add witness with height {}", height);
            }
        }
        if (witnessMap.size() == CollectSignService.witnessNum) {
            Enumeration<Long> keys = witnessMap.keys();
            List<Long> heights = new LinkedList();
            while (keys.hasMoreElements()) {
                heights.add(keys.nextElement());
            }
            boolean highestScore = SignBlockScoreStrategy.isAbsoluteHighestScore(blockHeight, heights);
            LOGGER.info("the sign score is highest for blockHeight", blockHeight, heights);
            if (!highestScore) {
                long count = countDownLatch.getCount();
                for (long i = 0; i < count; i++) {
                    countDownLatch.countDown();
                }
                return;
            }
        }
        countDownLatch.countDown();
    }

    public Collection<WitnessSign> getWitnessSign() {
        return witnessMap.values();
    }
}
