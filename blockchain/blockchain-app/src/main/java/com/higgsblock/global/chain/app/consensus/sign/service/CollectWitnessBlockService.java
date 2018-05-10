package com.higgsblock.global.chain.app.consensus.sign.service;

import com.higgsblock.global.chain.app.Application;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.BlockWitness;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.service.BlockReqService;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author yuguojia
 * @date 2018/4/25
 */
@Component
@Slf4j
public class CollectWitnessBlockService {
    public static Integer ENOUGH_SIG_NUM = 7;
    public static Integer WITNESS_NUM = 11;
    public static Integer RETRY_REQ_MAX_NUM = 50;

    @Autowired
    private BlockService blockService;
    @Autowired
    private BlockReqService blockReqService;
    @Autowired
    private PeerManager peerManager;
    @Autowired
    private MessageCenter messageCenter;

    @Setter
    private long currentHeight = 1;

    @Setter
    private ExecutorService executorService = null;

    /**
     * key1: block hash
     * key2: witnessAddress
     */
    private Map<String, Map<String, Block>> witnessBlockMaps;

    public void reInit(long currentHeight, ExecutorService executorService) {
        this.currentHeight = currentHeight;
        this.executorService = executorService;
        this.witnessBlockMaps = new ConcurrentHashMap<>();
        if (currentHeight <= Application.PRE_BLOCK_COUNT || executorService == null) {
            throw new RuntimeException("error param.");
        }
    }

    public boolean collectAllSignedBlockAndBroadcast() {
        int retryNum = 0;
        LOGGER.info("begin to collectAllSignedBlockAndBroadcast height={}", currentHeight);
        while (retryNum++ < RETRY_REQ_MAX_NUM) {
            collectAllSignedBlock(retryNum);
            if (!needContinueCollectSign()) {
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                LOGGER.error("thread sleep InterruptedException", e);
            }
        }
        String collectedInfo = getAllSignInfo();
        LOGGER.info("collected signed blocks info: "+collectedInfo);

        if (blockService.getBestMaxHeight() >= currentHeight) {
            LOGGER.info("has best block,do not need to merge sig,height={}", currentHeight);
            return false;
        }
        int countSignedMaxNum = countSignedMaxNum();
        if (countSignedMaxNum < ENOUGH_SIG_NUM) {
            LOGGER.error("cannot collect enough signatures height={}, stop application", currentHeight);
            System.exit(0);
            return false;
        }

        checkAndPrint();

        Block finalBlock = mergeFinalBlock();
        if (finalBlock == null || finalBlock.getOtherWitnessSigPKS().size() < ENOUGH_SIG_NUM) {
            LOGGER.error("cannot collect final block : "+finalBlock);
            return false;
        }
        messageCenter.dispatch(finalBlock);
        LOGGER.info("end to collectAllSignedBlockAndBroadcast height={}_block={}", finalBlock.getHeight(), finalBlock.getHash());
        return true;
    }

    private void collectAllSignedBlock(int retryNum) {
        List<Peer> peers = getNoSignedWitness();

        Collection callables = new LinkedList<>();
        LOGGER.info("to collect signed block,from retry={}_peers={}", retryNum, Peer.getPeersIds(peers));
        for (Peer peer : peers) {
            Callable<Block> signedBlock = () -> {
                try {
                    Block block = blockReqService.getRecommendBlock(peer.getIp(), peer.getHttpServerPort(), currentHeight);
                    boolean success = blockService.validRecommendBlock(block);
                    LOGGER.info("collected the signed block from {} for  retry={}_success={}_height={}_block={}", peer.getId(),
                            retryNum, success, block == null ? null : block.getHeight(), block == null ? null : block.getHash());
                    return success ? block : null;
                } catch (Throwable e) {
                    LOGGER.error("collected the signed block exception", e);
                }
                return null;
            };
            callables.add(signedBlock);
        }

        if (callables.isEmpty()) {
            return;
        }
        List<Future<Block>> futureList = null;
        try {
            futureList = executorService.invokeAll(callables, 3, TimeUnit.SECONDS);
            for (Future<Block> future : futureList) {
                Block block = null;
                try {
                    block = future.get(5, TimeUnit.SECONDS);
                } catch (Throwable e) {
                    LOGGER.error("future get data exception", e);
                } finally {
                    future.cancel(true);
                }
                addBlock(block);
            }
        } catch (Exception e) {
            LOGGER.info("collectAllSignedBlock get all future Exception", e);
        }
    }

    private boolean needContinueCollectSign() {
        if (countSignedMaxNum() >= ENOUGH_SIG_NUM) {
            return false;
        }
        if (!mayHasEnoughSameBlockSig()) {
            return false;
        }
        if (countSignedAllNum() >= WITNESS_NUM) {
            return false;
        }

        if (blockService.getBestMaxHeight() >= currentHeight) {
            return false;
        }
        return true;
    }

    private List<Peer> getNoSignedWitness() {
        List<Peer> peers = peerManager.getByIds(BlockService.WITNESS_ADDRESS_LIST.toArray(new String[0]));
        if (BlockService.WITNESS_ADDRESS_LIST.size() != peers.size()) {
            LOGGER.error("cannot get enough peers from peerManager, size={}", peers.size());
        }
        for (int i = peers.size() - 1; i >= 0; i--) {
            if (containsWitness(peers.get(i).getId())) {
                peers.remove(i);
            }
        }
        return peers;
    }

    private void checkAndPrint() {
        boolean hasEnoughSameBlockSig = hasEnoughSameBlockSig();
        int countSignedAllNum = countSignedAllNum();
        if (!hasEnoughSameBlockSig || countSignedAllNum > WITNESS_NUM) {
            LOGGER.error("collected signed blocks has error, enough={}_num={}",
                    hasEnoughSameBlockSig, countSignedAllNum);
        }
    }

    private Block mergeFinalBlock() {
        String signedMaxBlockHash = getSignedMaxBlockHash();
        Block finalBlock = mergeBlockSigns(signedMaxBlockHash);
        return finalBlock;
    }

    private Block mergeBlockSigns(String blockHash) {
        if (blockHash == null) {
            return null;
        }
        Block mergedBlock = null;
        Map<String, Block> sameBlockMap = witnessBlockMaps.get(blockHash);
        if (sameBlockMap == null || sameBlockMap.size() == 0) {
            return null;
        }
        Collection<Block> blocks = sameBlockMap.values();
        for (Block block : blocks) {
            if (mergedBlock == null) {
                mergedBlock = SerializationUtils.clone(block);
                continue;
            }
            List<BlockWitness> otherWitnessSigPKS = mergedBlock.getOtherWitnessSigPKS();
            otherWitnessSigPKS.add(block.getOtherWitnessSigPKS().get(0));
        }
        return mergedBlock;
    }

    public void addBlock(Block block) {
        if (block == null) {
            return;
        }
        if (currentHeight != block.getHeight()) {
            LOGGER.error("get error block height when collectAllSignedBlock");
            return;
        }
        String blockHash = block.getHash();
        Map<String, Block> sameBlockMap = witnessBlockMaps.computeIfAbsent(blockHash,
                s -> new ConcurrentHashMap<>());
        String witnessAddress = block.getOtherWitnessSigPKS().get(0).getAddress();
        Block otherBlock = checkWitnessHasOtherSignBlock(block);
        if (otherBlock != null) {
            LOGGER.error("there has another block signed by this witness:" +
                    "witness={},oldBlock={},newBlock={}", witnessAddress, otherBlock, block);
        }
        if (!sameBlockMap.containsKey(witnessAddress)) {
            sameBlockMap.put(witnessAddress, block);
            LOGGER.info("add witness={} recommend block={} to map.", witnessAddress,blockHash);
        }
    }

    private Block checkWitnessHasOtherSignBlock(Block block) {
        String blockHash = block.getHash();
        Set<String> keySet = witnessBlockMaps.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Map<String, Block> sameBlockMap = witnessBlockMaps.get(key);
            String witnessAddress = block.getOtherWitnessSigPKS().get(0).getAddress();
            Block otherBlock = sameBlockMap.get(witnessAddress);
            if (otherBlock != null && !StringUtils.equals(key, blockHash)) {
                return otherBlock;
            }
        }
        return null;
    }

    private boolean containsWitness(String witnessAddress) {
        Set<String> keySet = witnessBlockMaps.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            Map<String, Block> sameBlockMap = witnessBlockMaps.get(iterator.next());
            if (sameBlockMap.containsKey(witnessAddress)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEnoughSameBlockSig() {
        Set<String> keySet = witnessBlockMaps.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            Map<String, Block> sameBlockMap = witnessBlockMaps.get(iterator.next());
            if (sameBlockMap.size() >= ENOUGH_SIG_NUM) {
                return true;
            }
        }
        return false;
    }

    private int countSignedAllNum() {
        int result = 0;
        Set<String> keySet = witnessBlockMaps.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            Map<String, Block> sameBlockMap = witnessBlockMaps.get(iterator.next());
            result += sameBlockMap.size();
        }
        return result;
    }

    private int countSignedMaxNum() {
        int max = 0;
        Set<String> keySet = witnessBlockMaps.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            Map<String, Block> sameBlockMap = witnessBlockMaps.get(iterator.next());
            if (max < sameBlockMap.size()) {
                max = sameBlockMap.size();
            }
        }
        return max;
    }

    private String getSignedMaxBlockHash() {
        int max = 0;
        String result = null;
        Set<String> keySet = witnessBlockMaps.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Map<String, Block> sameBlockMap = witnessBlockMaps.get(key);
            if (max < sameBlockMap.size()) {
                max = sameBlockMap.size();
                result = key;
            }
        }
        return result;
    }

    private boolean mayHasEnoughSameBlockSig() {
        if (hasEnoughSameBlockSig()) {
            return true;
        }
        int allSignedNum = countSignedAllNum();
        int noSignedNum = WITNESS_NUM - allSignedNum;
        int maxSignedNum = countSignedMaxNum();
        if (maxSignedNum + noSignedNum >= ENOUGH_SIG_NUM) {
            return true;
        }
        return false;
    }

    private String getAllSignInfo() {
        StringBuilder sb = new StringBuilder();
        int allNum = countSignedAllNum();
        sb.append("num:").append(allNum).append(":");
        Set<String> keySet = witnessBlockMaps.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Map<String, Block> sameBlockMap = witnessBlockMaps.get(key);
            sb.append(key).append(":").append(sameBlockMap.keySet()).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static void  main(String[] args){
        CollectWitnessBlockService collectWitnessBlockService = new CollectWitnessBlockService();
        collectWitnessBlockService.reInit(1, ExecutorServices.newFixedThreadPool("witnessTask", 3, 50));
        Block block = new Block();
        block.setHeight(1);
        block.setBlockTime(System.currentTimeMillis());
        block.addWitnessSignature("11","22",null);
        collectWitnessBlockService.addBlock(block);
        Block block2 = new Block();
        block2.setHeight(1);
        block2.setBlockTime(System.currentTimeMillis()+1);
        block2.addWitnessSignature("22","33",null);
        collectWitnessBlockService.addBlock(block2);
        String allSignInfo = collectWitnessBlockService.getAllSignInfo();
        System.out.println(allSignInfo);
    }
}
