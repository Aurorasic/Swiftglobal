package com.higgsblock.global.chain.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.SignaturePair;
import com.higgsblock.global.chain.app.common.ScoreRangeEnum;
import com.higgsblock.global.chain.app.dao.IDposRepository;
import com.higgsblock.global.chain.app.dao.entity.DposEntity;
import com.higgsblock.global.chain.app.service.IDposService;
import com.higgsblock.global.chain.app.service.IScoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yangyi
 * @deta 2018/5/24
 * @description
 */
@Slf4j
@Service
public class DposService implements IDposService {

    @Autowired
    private IDposRepository dposRepository;
    @Autowired
    private IScoreService scoreService;
    @Autowired
    private BlockService blockService;


    @Override
    public List<String> get(long sn) {
        DposEntity dposEntity = dposRepository.findBySn(sn);
        return null == dposEntity ? null : JSONObject.parseArray(dposEntity.getAddresses(), String.class);
    }

    @Override
    public void save(long sn, List<String> addresses) {
        dposRepository.save(new DposEntity(sn, JSONObject.toJSONString(addresses)));
    }

    /**
     * calculate next round lucky miners and persist
     *
     * @param toBeBestBlock
     * @param maxHeight
     * @return
     */
    @Override
    public List<String> calcNextDposNodes(Block toBeBestBlock, long maxHeight) {
        List<String> dposAddresses = Lists.newLinkedList();
        boolean isFirstOfRound = calculateStartHeight(toBeBestBlock.getHeight()) == toBeBestBlock.getHeight();
        LOGGER.info("the best block={},is the first for dpos?={}, height={}",
                toBeBestBlock.getSimpleInfo(), isFirstOfRound, maxHeight);

        //select the next dpos nodes when toBeBestBlock height is first of round
        if (!isFirstOfRound) {
            return dposAddresses;
        }
        long sn = calculateSn(maxHeight);
        boolean selectedNextGroup = isDposGroupSeleted(sn + 1L);
        if (selectedNextGroup) {
            LOGGER.info("next dpos group has selected,height={},sn+1:{}", maxHeight, (sn + 1L));
            return dposAddresses;
        }
        dposAddresses = calculateDposAddresses(toBeBestBlock, maxHeight);
        //persist selected nodes address although  it is empty
        if (dposAddresses == null) {
            dposAddresses = Lists.newLinkedList();
        }
        persistDposNodes(sn, dposAddresses);
        LOGGER.info("persisted new dpos addresses for height={}", maxHeight);
        return dposAddresses;
    }

    /**
     * find lucky miners address by round num
     *
     * @param sn
     * @return
     */
    @Override
    public List<String> getDposGroupBySn(long sn) {
        List<String> dposAddress = get(sn);
        List<String> result = new LinkedList<>();
        if (dposAddress != null) {
            result.addAll(dposAddress);
        }
        return result;
    }

    /**
     * find rest miner addresses by pre block hash
     *
     * @param preBlockHash
     * @return
     */
    @Override
    public List<String> getRestDposMinersByPreHash(String preBlockHash) {
        if (preBlockHash == null) {
            return Lists.newLinkedList();
        }
        Block preBlock = blockService.getBlockByHash(preBlockHash);

        if (preBlock == null) {
            LOGGER.warn("the block:{} not found", preBlockHash);
            return Lists.newLinkedList();
        }
        long height = preBlock.getHeight() + 1;

        long sn = calculateSn(height);
        List<String> dposGroupBySn = getDposGroupBySn(sn);
        if (CollectionUtils.isEmpty(dposGroupBySn)) {
            return Lists.newLinkedList();
        }
        long startHeight = calculateStartHeight(height);
        while (height-- > startHeight) {
            SignaturePair minerFirstPKSig = preBlock.getMinerSigPair();
            String address = minerFirstPKSig.getAddress();
            dposGroupBySn.remove(address);
            preBlock = blockService.getBlockByHash(preBlock.getPrevBlockHash());
        }
        return dposGroupBySn;
    }

    /**
     * validate the producer
     *
     * @param block
     * @return
     */
    @Override
    public boolean checkProducer(Block block) {
        SignaturePair minerPKSig = block.getMinerSigPair();
        if (minerPKSig == null || !minerPKSig.valid()) {
            LOGGER.warn("the miner signature is invalid:{}", block.getSimpleInfo());
            return false;
        }

        String address = minerPKSig.getAddress();
        List<String> currentGroup = getRestDposMinersByPreHash(block.getPrevBlockHash());
        boolean result = CollectionUtils.isNotEmpty(currentGroup) && currentGroup.contains(address);
        if (!result) {
            LOGGER.info("the miner should not produce the block:{},miner:{},dpos:{}", block.getSimpleInfo(), minerPKSig.toJson(), currentGroup);
            return false;
        }

        return result;
    }

    /**
     * check the miner should be lucky miner or not
     *
     * @param height
     * @param address
     * @param preBlockHash
     * @return
     */
    @Override
    public boolean canPackBlock(long height, String address, String preBlockHash) {
        List<String> dposNodes = getRestDposMinersByPreHash(preBlockHash);
        if (CollectionUtils.isEmpty(dposNodes)) {
            LOGGER.warn("the dpos node is empty with the height={}", height);
            return false;
        }
        boolean canPackBlock = dposNodes.contains(address);
        LOGGER.info("canPackBlock?={},height={},address={}, the dposNodes={}", canPackBlock, height, address, dposNodes);

        if (!dposNodes.contains(address)) {
            return false;
        }
        return true;
    }

    /**
     * calculate the start height in this round of the height
     *
     * @param height
     * @return
     */
    @Override
    public long calculateStartHeight(long height) {
        if (height <= 1) {
            return 1;
        }
        return (calculateSn(height) - 2) * DPOS_BLOCKS_PER_ROUND + 2L;
    }

    /**
     * calculate the end height in this round of the height
     *
     * @param height
     * @return
     */
    @Override
    public long calculateEndHeight(long height) {
        if (height <= 1) {
            return 1;
        }
        return (calculateSn(height) - 1) * DPOS_BLOCKS_PER_ROUND + 1L;
    }

    /**
     * calculate the round num by height
     *
     * @param height
     * @return
     */
    @Override
    public long calculateSn(long height) {
        if (height == 1L) {
            return 1L;
        }
        return (height - DPOS_START_HEIGHT) / DPOS_BLOCKS_PER_ROUND + 2L;
    }

    /**
     * check block unstrictly,if the miner is constained in the dpos miners,return true
     *
     * @param block
     * @return
     */
    @Override
    public boolean checkBlockUnstrictly(Block block) {
        if (block == null || block.getMinerSigPair() == null) {
            return false;
        }
        List<String> miners = getDposGroupBySn(calculateSn(block.getHeight()));
        if (CollectionUtils.isEmpty(miners)) {
            return false;
        }
        String miner = block.getMinerSigPair().getAddress();
        return miners.contains(miner);
    }

    private List<String> calculateDposAddresses(Block toBeBestBlock, long maxHeight) {
        List<String> selected = Lists.newLinkedList();
        long sn = calculateSn(maxHeight);
        final String hash = toBeBestBlock.getHash();
        LOGGER.debug("begin to select dpos node,bestblock:{},height={}", toBeBestBlock.getSimpleInfo(), maxHeight);
        final List<String> currentGroup = getDposGroupBySn(sn);
        LOGGER.info("the currentGroup is {}", currentGroup);

        // group by score range
        List<String> level5List = scoreService.queryAddresses(ScoreRangeEnum.LEVEL5_SCORE, currentGroup);
        List<String> level4List = scoreService.queryAddresses(ScoreRangeEnum.LEVEL4_SCORE, currentGroup);
        List<String> level3List = scoreService.queryAddresses(ScoreRangeEnum.LEVEL3_SCORE, currentGroup);
        List<String> level2List = scoreService.queryAddresses(ScoreRangeEnum.LEVEL2_SCORE, currentGroup);
        List<String> level1List = scoreService.queryAddresses(ScoreRangeEnum.LEVEL1_SCORE, currentGroup);
        LOGGER.debug("height={} for select {} round dpos node from level5List:{},level4List:{},level3List:{},level2List:{},level1List:{}",
                maxHeight, (sn + 1), level5List, level4List, level3List, level2List, level1List);

        // Shuffle by miner address and block hash
        HashFunction function = Hashing.sha256();
        Comparator<String> comparator = (o1, o2) -> {
            HashCode hashCode1 = function.hashString(o1 + hash, Charsets.UTF_8);
            HashCode hashCode2 = function.hashString(o2 + hash, Charsets.UTF_8);
            return hashCode1.toString().compareTo(hashCode2.toString());
        };
        level5List.sort(comparator);
        level4List.sort(comparator);
        level3List.sort(comparator);
        level2List.sort(comparator);
        level1List.sort(comparator);

        // Select miners by score range
        int level5Size = ScoreRangeEnum.LEVEL5_SCORE.getSelectSize();
        int level4Size = ScoreRangeEnum.LEVEL4_SCORE.getSelectSize();
        int level3Size = ScoreRangeEnum.LEVEL3_SCORE.getSelectSize();
        int level2Size = ScoreRangeEnum.LEVEL2_SCORE.getSelectSize();

        selected.addAll(level5List.stream().limit(level5Size).collect(Collectors.toList()));
        selected.addAll(level4List.stream().limit(level4Size).collect(Collectors.toList()));
        selected.addAll(level3List.stream().limit(level3Size).collect(Collectors.toList()));
        selected.addAll(level2List.stream().limit(level2Size).collect(Collectors.toList()));

        int size = level5Size + level4Size + level3Size + level2Size - selected.size();
        if (size <= 0) {
            LOGGER.info("first select the dpos node is {},sn+1:{},height={}", selected, (sn + 1), maxHeight);
            return selected;
        }

        // If the selected miners are not enough, then select others from the left miners.
        List<String> left = Lists.newLinkedList();
        left.addAll(level5List);
        left.addAll(level4List);
        left.addAll(level3List);
        left.addAll(level2List);
        left.addAll(level1List);
        left.removeAll(selected);
        selected.addAll(left.stream().limit(size).collect(Collectors.toList()));
        LOGGER.info("the dpos node is sn+1:{}->{},height={}", (sn + 1), selected, maxHeight);
        if (selected.size() < NODE_SIZE) {
            LOGGER.warn("can not find enough dpos node,sn+1:{}->{}", (sn + 1), selected);
        }
        return selected;
    }

    private void persistDposNodes(long sn, List<String> dposNodes) {
        save(sn + 1, dposNodes);
        LOGGER.info("persist dposNode,sn+1:{},dposNode:{}", sn + 1, dposNodes);
    }

    private boolean isDposGroupSeleted(long sn) {
        DposEntity dposEntity = dposRepository.findBySn(sn);
        return dposEntity != null;
    }

}
