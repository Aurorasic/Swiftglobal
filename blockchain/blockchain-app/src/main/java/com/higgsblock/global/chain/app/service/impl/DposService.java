package com.higgsblock.global.chain.app.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockWitness;
import com.higgsblock.global.chain.app.dao.IDposRepository;
import com.higgsblock.global.chain.app.dao.entity.DposEntity;
import com.higgsblock.global.chain.app.dao.entity.ScoreEntity;
import com.higgsblock.global.chain.app.service.IDposService;
import com.higgsblock.global.chain.app.service.IScoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yangyi
 * @deta 2018/5/24
 * @description
 */
@Slf4j
@Service
public class DposService implements IDposService, InitializingBean {


    private final int maxScore = 1000;
    private final int midScore = 800;
    private final int mixScore = 600;
    @Autowired
    private IDposRepository dposRepository;
    @Autowired
    private IScoreService scoreService;
    @Autowired
    private BlockService blockService;
    private Cache<Long, List<String>> dposNodeMap = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();
    private Function<Long, List<String>> function = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        function = (sn) -> get(sn);
    }

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
        boolean isFirstOfRound = getStartHeight(toBeBestBlock.getHeight()) == toBeBestBlock.getHeight();

        //select the next dpos nodes when toBeBestBlock height is first of round
        if (!isFirstOfRound) {
            return dposAddresses;
        }
        LOGGER.info("toBeBestBlcok:{} is the first of this round,select next dpos nodes", toBeBestBlock.getSimpleInfo());
        long sn = getSn(maxHeight);
        boolean selectedNextGroup = isDposGroupSeleted(sn + 1L);
        if (selectedNextGroup) {
            LOGGER.info("next dpos group has selected,blockheight:{},sn+1:{}", maxHeight, (sn + 1L));
            return dposAddresses;
        }
        dposAddresses = calculateDposAddresses(toBeBestBlock, maxHeight);
        //persist selected nodes address although  it is empty
        if (dposAddresses == null) {
            dposAddresses = Lists.newLinkedList();
        }
        persistDposNodes(sn, dposAddresses);
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
        List<String> dposAddress = dposNodeMap.get(sn, function);
        List<String> result = new LinkedList<>();
        if (dposAddress != null) {
            result.addAll(dposAddress);
        }
        return result;
    }

    /**
     * find lucky miners by pre blockhash
     *
     * @param preBlockHash
     * @return
     */
    @Override
    public List<String> getDposGroupByPreBlockHash(String preBlockHash) {
        if (preBlockHash == null) {
            return Lists.newLinkedList();
        }
        Block preBlock = blockService.getBlockByHash(preBlockHash);

        if (preBlock == null) {
            LOGGER.warn("the block:{} not found", preBlockHash);
            return Lists.newLinkedList();
        }
        long height = preBlock.getHeight() + 1;

        long sn = getSn(height);
        List<String> dposGroupBySn = getDposGroupBySn(sn);
        if (CollectionUtils.isEmpty(dposGroupBySn)) {
            return Lists.newLinkedList();
        }
        long startHeight = getStartHeight(height);
        while (height-- > startHeight) {
            BlockWitness minerFirstPKSig = preBlock.getMinerFirstPKSig();
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
        BlockWitness minerPKSig = block.getMinerFirstPKSig();
        if (minerPKSig == null || !minerPKSig.valid()) {
            LOGGER.warn("the miner signature is invalid:{}", block.getSimpleInfo());
            return false;
        }

        String address = minerPKSig.getAddress();
        List<String> currentGroup = getDposGroupByPreBlockHash(block.getPrevBlockHash());
        boolean result = CollectionUtils.isNotEmpty(currentGroup) && currentGroup.contains(address);
        if (!result) {
            LOGGER.error("the miner should not produce the block:{},miner:{},dpos:{}", block.getSimpleInfo(), minerPKSig.toJson(), currentGroup);
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
        long startHeight = getStartHeight(height);
        if (startHeight > height) {
            throw new RuntimeException("the batchStartHeight should not be smaller than the height,the batchStartHeight " + startHeight + ",the height=" + height);
        }
        List<String> dposNodes = getDposGroupByPreBlockHash(preBlockHash);
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
    public long getStartHeight(long height) {
        if (height <= 1) {
            return 1;
        }
        return (getSn(height) - 2) * DPOS_BLOCKS_PER_ROUND + 2L;
    }

    /**
     * calculate the end height in this round of the height
     *
     * @param height
     * @return
     */
    @Override
    public long getEndHeight(long height) {
        if (height <= 1) {
            return 1;
        }
        return (getSn(height) - 1) * DPOS_BLOCKS_PER_ROUND + 1L;
    }

    /**
     * calculate the round num by height
     *
     * @param height
     * @return
     */
    @Override
    public long getSn(long height) {
        if (height == 1L) {
            return 1L;
        }
        return (height - DPOS_START_HEIGHT) / DPOS_BLOCKS_PER_ROUND + 2L;
    }


    private List<String> calculateDposAddresses(Block toBeBestBlock, long maxHeight) {
        List<String> selected = Lists.newLinkedList();
        long sn = getSn(maxHeight);
        List<ScoreEntity> all = scoreService.all();
        LOGGER.info("select {} round dpos node from dposMinerScore:{}", (sn + 1), all);
        if (CollectionUtils.isEmpty(all)) {
            return selected;
        }
        final String hash = toBeBestBlock.getHash();
        LOGGER.info("begin to select dpos node,the bestblock hash is {},bestblock height is {}", hash, toBeBestBlock.getHeight());
        final List<String> currentGroup = getDposGroupBySn(sn);
        LOGGER.info("the currentGroup is {}", currentGroup);

        // group by score range
        List<String> maxScoreList = Lists.newLinkedList();
        List<String> midScoreList = Lists.newLinkedList();
        List<String> minScoreList = Lists.newLinkedList();
        List<String> inadequateScoreList = Lists.newLinkedList();

        all.forEach(entity -> {
            if (null == entity) {
                return;
            }
            Integer score = entity.getScore();
            String address = entity.getAddress();
            if (null == score || StringUtils.isBlank(address)) {
                return;
            }
            if (currentGroup.contains(address)) {
                return;
            }
            if (score >= maxScore) {
                maxScoreList.add(address);
                return;
            }
            if (score >= midScore) {
                midScoreList.add(address);
                return;
            }
            if (score >= mixScore) {
                minScoreList.add(address);
                return;
            }
            inadequateScoreList.add(address);
        });

        // Shuffle by miner address and block hash
        HashFunction function = Hashing.sha256();
        Comparator<String> comparator = (o1, o2) -> {
            HashCode hashCode1 = function.hashString(o1 + hash, Charsets.UTF_8);
            HashCode hashCode2 = function.hashString(o2 + hash, Charsets.UTF_8);
            return hashCode1.toString().compareTo(hashCode2.toString());
        };
        maxScoreList.sort(comparator);
        midScoreList.sort(comparator);
        minScoreList.sort(comparator);

        // Select miners by score range
        int maxSize = 3;
        int midSize = 2;
        int minSize = 1;

        selected.addAll(maxScoreList.stream().limit(maxSize).collect(Collectors.toList()));
        selected.addAll(midScoreList.stream().limit(midSize).collect(Collectors.toList()));
        selected.addAll(minScoreList.stream().limit(minSize).collect(Collectors.toList()));
        int size = maxSize + midSize + minSize - selected.size();
        if (size <= 0) {
            LOGGER.info("first select the dpos node is {},sn+1:{}", selected, (sn + 1));
            return selected;
        }

        // If the selected miners are not enough, then select others from the left miners.
        List<String> left = Lists.newLinkedList();
        left.addAll(maxScoreList);
        left.addAll(midScoreList);
        left.addAll(minScoreList);
        left.addAll(inadequateScoreList);
        left.removeAll(selected);
        selected.addAll(left.stream().limit(size).collect(Collectors.toList()));
        LOGGER.info("the dpos node is {},sn+1:{}", selected, (sn + 1));
        if (selected.size() < NODE_SIZE) {
            LOGGER.warn("can not find enough dpos node:{},sn+1:{}", selected, (sn + 1));
        }
        return selected;
    }

    private void persistDposNodes(long sn, List<String> dposNodes) {
        dposNodeMap.put(sn + 1, dposNodes);
        save(sn + 1, dposNodes);
        LOGGER.info("persist dposNode,sn+1:{},dposNode:{}", sn + 1, dposNodes);
    }

    private boolean isDposGroupSeleted(long sn) {
        DposEntity dposEntity = dposRepository.findBySn(sn);
        return dposEntity != null;
    }

}
