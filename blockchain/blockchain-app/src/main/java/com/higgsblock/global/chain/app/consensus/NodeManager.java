package com.higgsblock.global.chain.app.consensus;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockWitness;
import com.higgsblock.global.chain.app.service.IScoreService;
import com.higgsblock.global.chain.app.service.impl.BlockDaoService;
import com.higgsblock.global.chain.app.service.impl.DposService;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author yangyi
 * @deta 2018/3/7
 * @description
 */
@Component
@Slf4j
public class NodeManager implements InitializingBean {

    public static final int NODE_SIZE = 6;
    public static final int MAX_SIZE = 30;
    public static final int CONFIRM_BEST_BLOCK_MIN_NUM = 3;
    public static final int DPOS_BLOCKS_PER_ROUND = 5;
    public static final long DPOS_START_HEIGHT = 2L;
    private final int maxScore = 1000;
    private final int midScore = 800;
    private final int mixScore = 600;
    @Autowired
    private IScoreService scoreDaoService;
    @Autowired
    private BlockDaoService blockDaoService;

    @Autowired
    private DposService dposService;
    private Cache<Long, List<String>> dposNodeMap = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();
    private Function<Long, List<String>> function = null;

    public void calculateDposNodes(Block toBeBestBlock, long maxHeight) {
        List<String> dposAddresses = calculateDposAddresses(toBeBestBlock, maxHeight);
        if (CollectionUtils.isEmpty(dposAddresses)) {
            return;
        }
        long sn = getSn(maxHeight);
        persistDposNodes(sn, dposAddresses);
    }

    public List<String> calculateDposAddresses(Block toBeBestBlock, long maxHeight) {

        boolean isFirstOfRound = getBatchStartHeight(toBeBestBlock.getHeight()) == toBeBestBlock.getHeight();

        //select the next dpos nodes when toBeBestBlock height is first of round
        if (!isFirstOfRound) {
            return null;
        }
        LOGGER.info("toBeBestBlcok:{} is the first of this round,select next dpos nodes", toBeBestBlock.getSimpleInfo());
        long sn = getSn(maxHeight);
        boolean selectedNextGroup = isGroupSeleted(sn + 1L);
        if (selectedNextGroup) {
            LOGGER.info("next dpos group has selected,blockheight:{},sn+1:{}", maxHeight, (sn + 1L));
            return null;
        }

        Map<String, Integer> dposMinerSoreMap = scoreDaoService.loadAll();
        LOGGER.info("select {} dpos node from dposMinerScore:{}", (sn + 1), dposMinerSoreMap);
        if (dposMinerSoreMap.size() < NODE_SIZE) {
            return null;
        }
        final String hash = toBeBestBlock.getHash();
        LOGGER.info("begin to select dpos node,the bestblock hash is {},bestblock height is {}", hash, toBeBestBlock.getHeight());
        final List<String> currentGroup = getDposGroupBySn(sn);
        LOGGER.info("the currentGroup is {}", currentGroup);
        List<String> maxScoreList = new ArrayList<>();
        List<String> midScoreList = new ArrayList<>();
        List<String> minScoreList = new ArrayList<>();
        List<String> inadequateScoreList = new ArrayList<>();
        dposMinerSoreMap.forEach((address, score) -> {
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
        int maxSize = 3;
        int midSize = 2;
        int minSize = 1;
        HashFunction function = Hashing.sha256();
        Comparator<String> comparator = (o1, o2) -> {
            o1 = o1 + hash;
            o2 = o2 + hash;
            HashCode hashCode1 = function.hashString(o1, Charset.forName("UTF-8"));
            HashCode hashCode = function.hashString(o2, Charset.forName("UTF-8"));
            return hashCode1.toString().compareTo(hashCode.toString());
        };
        List<String> select = maxScoreList.stream().sorted(comparator).limit(maxSize).collect(Collectors.toList());
        select.addAll(midScoreList.stream().sorted(comparator).limit(midSize).collect(Collectors.toList()));
        select.addAll(minScoreList.stream().sorted(comparator).limit(minSize).collect(Collectors.toList()));
        int size = maxSize + midSize + minSize - select.size();
        if (size <= 0) {
            LOGGER.info("first select the dpos node is {},sn+1:{}", select, (sn + 1));
            return select;
        }
        List<String> list = new LinkedList();
        list.addAll(maxScoreList);
        list.addAll(midScoreList);
        list.addAll(minScoreList);
        list.addAll(inadequateScoreList);
        list.removeAll(select);
        select.addAll(list.stream().sorted(comparator).limit(size).collect(Collectors.toList()));
        LOGGER.info("the dpos node is {},sn+1:{}", select, (sn + 1));
        if (select.size() < NODE_SIZE) {
            throw new RuntimeException("can not find enough dpos node,sn+1:" + (sn + 1));
        }
        return select;
    }

    public void persistDposNodes(long sn, List<String> dposNodes) {
        dposNodeMap.put(sn + 1, dposNodes);
        dposService.put(sn + 1, dposNodes);
        LOGGER.info("persist dposNode,sn+1:{},dposNode:{}", sn + 1, dposNodes);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        function = (sn) -> dposService.get(sn);
    }

    public List<String> getDposGroupBySn(long sn) {
        List<String> dposAddress = dposNodeMap.get(sn, function);
        List<String> result = new LinkedList<>();
        if (dposAddress != null) {
            result.addAll(dposAddress);
        }
        return result;
    }

    /**
     * find dpos miners by height,exceed miners which have produced blocks at the branch
     *
     * @param height
     * @param preBlockHash
     * @return
     */
    public List<String> getDposGroupByHeihgt(long height, String preBlockHash) {
        long sn = getSn(height);
        List<String> dposGroupBySn = getDposGroupBySn(sn);
        if (CollectionUtils.isEmpty(dposGroupBySn)) {
            return new ArrayList<>();
        }
        long startHeight = getBatchStartHeight(height);
        while (height-- > startHeight) {
            Block preBlock = blockDaoService.getBlockByHash(preBlockHash);
            BlockWitness minerFirstPKSig = preBlock.getMinerFirstPKSig();
            String address = minerFirstPKSig.getAddress();
            dposGroupBySn.remove(address);
            preBlockHash = preBlock.getPrevBlockHash();
        }
        return dposGroupBySn;
    }

    public boolean checkProducer(Block block) {
        BlockWitness blockWitness = block.getMinerFirstPKSig();
        if (blockWitness == null || StringUtils.isEmpty(blockWitness.getPubKey())) {
            return false;
        }
        String address = ECKey.pubKey2Base58Address(blockWitness.getPubKey());
        List<String> currentGroup = getDposGroupByHeihgt(block.getHeight(), block.getPrevBlockHash());
        return CollectionUtils.isNotEmpty(currentGroup) && currentGroup.contains(address);
    }

    public long getBatchStartHeight(long height) {
        if (height <= 1) {
            return 1;
        }
        return (getSn(height) - 2) * DPOS_BLOCKS_PER_ROUND + 2L;
    }

    public boolean canPackBlock(long height, String address, String preBlockHash) {
        long batchStartHeight = getBatchStartHeight(height);
        if (batchStartHeight > height) {
            throw new RuntimeException("the batchStartHeight should not be smaller than the height,the batchStartHeight " + batchStartHeight + ",the height=" + height);
        }
        List<String> dposNodes = getDposGroupByHeihgt(height, preBlockHash);
        if (CollectionUtils.isEmpty(dposNodes)) {
            LOGGER.error("the dpos node is empty with the height={}", height);
            return false;
        }
        boolean canPackBlock = dposNodes.contains(address);
        LOGGER.info("canPackBlock?={},height={},address={}, the dposNodes={}", canPackBlock, height, address, dposNodes);

        if (!dposNodes.contains(address)) {
            return false;
        }
        return true;
    }


    public long getSn(long height) {
        if (height == 1L) {
            return 1L;
        }
        return (height - DPOS_START_HEIGHT) / DPOS_BLOCKS_PER_ROUND + 2L;
    }

    public boolean isGroupSeleted(long sn) {
        return CollectionUtils.isNotEmpty(getDposGroupBySn(sn));
    }

}
