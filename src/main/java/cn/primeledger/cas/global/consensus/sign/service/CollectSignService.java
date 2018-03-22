package cn.primeledger.cas.global.consensus.sign.service;

import cn.primeledger.cas.global.Application;
import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.blockchain.BlockWitness;
import cn.primeledger.cas.global.blockchain.PreMiningService;
import cn.primeledger.cas.global.blockchain.transaction.Transaction;
import cn.primeledger.cas.global.blockchain.transaction.TransactionCacheManager;
import cn.primeledger.cas.global.common.entity.UnicastMessageEntity;
import cn.primeledger.cas.global.common.event.UnicastEvent;
import cn.primeledger.cas.global.consensus.SignBlockScoreStrategy;
import cn.primeledger.cas.global.consensus.sign.handler.BlockerCollectSignHandler;
import cn.primeledger.cas.global.consensus.sign.handler.WitnessSignHandler;
import cn.primeledger.cas.global.consensus.sign.model.WitnessSign;
import cn.primeledger.cas.global.crypto.ECKey;
import cn.primeledger.cas.global.crypto.model.KeyPair;
import cn.primeledger.cas.global.p2p.Peer;
import cn.primeledger.cas.global.service.BlockReqService;
import cn.primeledger.cas.global.service.PeerReqService;
import cn.primeledger.cas.global.utils.ExecutorServices;
import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;

import static cn.primeledger.cas.global.constants.EntityType.BLOCK_CREATE_SIGN;

//import cn.primeledger.cas.global.p2p.RegisterCenter;


/**
 * The collecting service responsible for signing the block and sending it to
 * the other witness peers for resigning the block. After other witness peers
 * resigning it {@link WitnessSignHandler}, they will send back the resigning
 * signs which be handled by {@link BlockerCollectSignHandler}.
 *
 * @author zhao xiaogang
 * @date 2018/3/6
 */
@Component
@Slf4j
public class CollectSignService {

    private static final int MAX_CACHE_SIZE = 100;

    @Autowired
    private KeyPair peerKeyPair;

    @Autowired
    private BlockService blockService;

    @Resource(name = "witnessedBlock")
    private ConcurrentMap<Long, Block> witnessedBlock;

    @Autowired
    private TransactionCacheManager txCacheManager;

    @Autowired
    private PreMiningService preMiningService;

    public final static int witnessNum = 3;

    public final static int WITNESSBATCHNUM = 12;

    @Autowired
    private PeerReqService peerReqService;

    @Autowired
    private BlockReqService blockReqService;

    private ExecutorService executorService = ExecutorServices.newFixedThreadPool("blockTimer", 3, 100);

    private Cache<String, String> witnessCache = Caffeine.
            newBuilder().maximumSize(MAX_CACHE_SIZE).build();

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
        LOGGER.info("for {} height, validate sign height gap set is: {}", height, result);

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
                return result;
            }
            BlockWitness minerPKSig = block.getMinerFirstPKSig();
            String pubKey = minerPKSig.getPubKey();
            if (StringUtils.isBlank(pubKey)) {
                continue;
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
        block.initMinerPkSig(peerKeyPair.getPubKey(), sig);
        WitnessSign sign = new WitnessSign(block.getHash(), sig, peerKeyPair.getPubKey());

        LOGGER.info("Create witness sign : {}", JSON.toJSONString(sign));
        return sign;
    }

    /**
     * Creator sends the signed block to other witnesses for resigning.
     */
    public Block sendBlockToWitness() {
        List<String> addresses = new ArrayList<>();
        addresses.add(ECKey.pubKey2Base58Address("028a186b944c76d7ca626a3ba8ba9609d46de318affb48ee760a0c3336f426d741"));
        Transaction transaction = preMiningService.buildMinerJoinTx(addresses, 0L, (short) 1, BigDecimal.ONE);
        txCacheManager.addTransaction(transaction);
        Block block = blockService.packageNewBlock();
        LOGGER.info("pack new block {}", block);
        if (block == null) {
            return block;
        }
        long height = block.getHeight();
        if (height > Application.PRE_BLOCK_COUNT) {
            Map<String, Map<String, Long>> prevRoundWitnessMap = findPrevRoundWitness(height);
            LOGGER.info("find pre round witness success for block with height {} ,result is {}", height, prevRoundWitnessMap);
            String[] strings = prevRoundWitnessMap.keySet().toArray(new String[]{});
            if (strings == null || strings.length < 3) {
                LOGGER.warn("can not find enough address {}", height);
                return null;
            }

            List<Peer> peers = peerReqService.doGetPeerListRequest(strings);
            if (peers == null || peers.size() < 3) {
                LOGGER.warn("can not find enough peers {}", height);
                return null;
            }

            ConcurrentHashMap<Long, WitnessSign> witnessMap = new ConcurrentHashMap<Long, WitnessSign>();

            Collection callables = new LinkedList<>();
            for (Peer peer : peers) {
                Callable<WitnessSign> witnessSign = () -> {
                    try {
                        WitnessSign data = blockReqService.getWitnessSign(peer.getIp(), peer.getHttpServerPort(), block);
                        return data;
                    } catch (Throwable e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                };
                callables.add(witnessSign);
            }
            List<Future<WitnessSign>> list = null;
            try {
                list = executorService.invokeAll(callables, 5, TimeUnit.SECONDS);
                for (Future<WitnessSign> future : list) {
                    WitnessSign data = null;
                    try {
                        data = future.get();
                    } catch (Throwable e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    if (data == null || StringUtils.isBlank(data.getSignature())) {
                        continue;
                    }
                    long blockHeight = block.getHeight();
                    LOGGER.info("receive witness {} for block with height", data, blockHeight);
                    String pubKey = data.getPubKey();
                    String address = ECKey.pubKey2Base58Address(pubKey);
                    Map<String, Long> stringLongMap = prevRoundWitnessMap.get(address);
                    Long signHeight = stringLongMap.values().iterator().next();
                    data.setBlockHash(stringLongMap.keySet().iterator().next());
                    if (signHeight == null) {
                        LOGGER.error("receive witness {} but the signer is not right {}", data, blockHeight);
                        continue;
                    }
                    LOGGER.info("the signer's height is {} for blockHeight {}", signHeight, blockHeight);
                    boolean valid = blockService.validateWitness(block.getSignedHash(), pubKey, data.getSignature(), block);
                    if (!valid) {
                        LOGGER.info("receive witness but the sign is not right from blockHash", block.getSignedHash());
                        continue;
                    }
                    if (witnessMap.size() < CollectSignService.witnessNum) {
                        witnessMap.put(signHeight, data);
                        LOGGER.info("add witness with height {}", signHeight);
                    } else {
                        Long lastHeight = signHeight;
                        Enumeration<Long> keys = witnessMap.keys();
                        while (keys.hasMoreElements()) {
                            Long aLong = keys.nextElement();
                            if (lastHeight > aLong) {
                                lastHeight = aLong;
                            }
                        }
                        if (lastHeight.compareTo(signHeight) != 0) {
                            witnessMap.remove(lastHeight);
                            LOGGER.info("remove witness with height {}", lastHeight);
                            witnessMap.put(signHeight, data);
                            LOGGER.info("add witness with height {}", signHeight);
                        }
                    }
                }
            } catch (InterruptedException e) {
                LOGGER.info(e.getMessage(), e);
            }
//            if (witnessMap.size() != CollectSignService.witnessNum) {
//                LOGGER.warn("can not collect enough sign and return null {}", height);
//                return null;
//            }

            Collection<WitnessSign> witnessSigns = witnessMap.values();
            LOGGER.info("collect enough sign and add sign to block {}", height);
            if (CollectionUtils.isNotEmpty(witnessSigns)) {
                witnessSigns.forEach(witnessSign -> {
                    block.addWitnessSignature(witnessSign.getPubKey(), witnessSign.getSignature(), witnessSign.getBlockHash());
                });
            }
            LOGGER.info("add minerSignature {}", height);
            final String blockHash = block.getSignedHash();
            final String sig = ECKey.signMessage(blockHash, peerKeyPair.getPriKey());
            block.addMinerSignature(peerKeyPair.getPubKey(), sig, blockHash);
        }
        LOGGER.info("persist the new block {}", height);
        boolean success = blockService.persistBlockAndIndex(block, null, (short) 1);
        LOGGER.info("persist {} height block {}", height, success);
        if (success) {
            blockService.broadCastBlock(block);
        }
        LOGGER.info("Send signed block : {}", JSON.toJSONString(block));
        return block;
    }

    /**
     * Witness sends the sign back to the block creator after creating the sign.
     * {@link CollectSignService#createSign(Block)}
     */
    public void sendSignToCreator(WitnessSign sign, String sourceId) {
        UnicastMessageEntity entity = new UnicastMessageEntity();
        entity.setType(BLOCK_CREATE_SIGN.getCode());
        entity.setVersion(sign.getVersion());
        entity.setData(JSON.toJSONString(sign));
        entity.setSourceId(sourceId);
        Application.EVENT_BUS.post(new UnicastEvent(entity));

        LOGGER.info("Send witness sign : {}", JSON.toJSONString(sign));
    }

    public boolean ifWitnessInCache(String key) {
        return StringUtils.isNotEmpty(witnessCache.getIfPresent(key));
    }
}
