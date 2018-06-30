package com.higgsblock.global.chain.app.consensus;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.Application;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.BlockWitness;
import com.higgsblock.global.chain.app.service.IScoreService;
import com.higgsblock.global.chain.app.service.impl.BlockIdxDaoService;
import com.higgsblock.global.chain.app.service.impl.DposService;
import com.higgsblock.global.chain.crypto.ECKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.rocksdb.RocksDBException;
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

    public static final int NODE_SIZE = 5;
    public static final int MAX_SIZE = 20;
    public static final int BATCH_BLOCK_NUM = 3;
    public static final long DPOS_START_HEIGHT = 2L;
    public static final long FIRST_HEIGHT_CAL_HEIGHT = 4L;
    private final int maxScore = 1000;
    private final int midScore = 800;
    private final int mixScore = 600;
    @Autowired
    private BlockService blockService;
    @Autowired
    private IScoreService scoreDaoService;

    @Autowired
    private BlockIdxDaoService blockIdxDaoService;
    @Autowired
    private DposService dposService;
    private Cache<Long, List<String>> dposNodeMap = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();
    private Function<Long, List<String>> function = null;

    public void calculateDposNodes(Block block) throws RocksDBException {
        List<String> dposAddresses = calculateDposAddresses(block);
        if (CollectionUtils.isEmpty(dposAddresses)) {
            return;
        }
        long sn = getSn(block.getHeight());
        persistDposNodes(sn, dposAddresses);
    }

    public List<String> calculateDposAddresses(Block block) throws RocksDBException {
        long height = block.getHeight();
        boolean isEndHeight = isEndHeight(height);
        if (!isEndHeight) {
            return null;
        }
        Map<String, Integer> dposMinerSoreMap = scoreDaoService.loadAll();
        if (dposMinerSoreMap.size() < NodeManager.NODE_SIZE) {
            return null;
        }
        long sn = getSn(height);
        final String hash = block.getHash();
        LOGGER.info("begin to select dpos node,the block hash is {}", hash);
        final List<String> currentGroup = getDposGroupBySn(sn);
        final List<String> nextGroup = getDposGroupBySn(sn + 1);
        LOGGER.info("the currentGroup is {}", currentGroup);
        List<String> maxScoreList = new ArrayList<>();
        List<String> midScoreList = new ArrayList<>();
        List<String> minScoreList = new ArrayList<>();
        List<String> inadequateScoreList = new ArrayList<>();
        dposMinerSoreMap.forEach((address, score) -> {
            if (currentGroup.contains(address) || nextGroup.contains(address)) {
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
        int maxSize = 2;
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
            return select;
        }
        List<String> list = new LinkedList();
        list.addAll(maxScoreList);
        list.addAll(midScoreList);
        list.addAll(minScoreList);
        list.addAll(inadequateScoreList);
        list.removeAll(select);
        select.addAll(list.stream().sorted(comparator).limit(size).collect(Collectors.toList()));
        LOGGER.info("the dpos node is {}", select);
        if (select.size() < BATCH_BLOCK_NUM) {
            throw new RuntimeException("can not find enough dpos node");
        }
        return select;
    }

    public void persistDposNodes(long sn, List<String> dposNodes) {
        dposNodeMap.put(sn + 2, dposNodes);
        dposService.put(sn + 2, dposNodes);
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

    public List<String> getDposGroupByHeihgt(long height) {
        long sn = getSn(height);
        List<String> dposGroupBySn = getDposGroupBySn(sn);
        if (CollectionUtils.isEmpty(dposGroupBySn)) {
            return new ArrayList<>();
        }
        long startHeight = getBatchStartHeight(height);
        for (long i = startHeight; i < height; i++) {
            Block block = blockService.getBestBlockByHeight(i);
            if (block == null) {
                continue;
            }
            BlockWitness minerFirstPKSig = block.getMinerFirstPKSig();
            String address = minerFirstPKSig.getAddress();
            dposGroupBySn.remove(address);
        }
        return dposGroupBySn;
    }

    public boolean checkProducer(Block block) {
        BlockWitness blockWitness = block.getMinerFirstPKSig();
        String address = ECKey.pubKey2Base58Address(blockWitness.getPubKey());
        List<String> currentGroup = this.getDposGroupByHeihgt(block.getHeight());
        return CollectionUtils.isNotEmpty(currentGroup) && currentGroup.contains(address);
    }

    public int getFullBlockCountByHeight(long height) {
        long mod = (height - Application.PRE_BLOCK_COUNT) % BATCH_BLOCK_NUM;
        if (mod == 1) {
            return NodeManager.NODE_SIZE;
        } else if (mod == 2) {
            return NodeManager.NODE_SIZE - 1;
        } else if (mod == 0) {
            return NodeManager.NODE_SIZE - 2;
        }
        throw new RuntimeException("height is error");
    }


    public boolean isEndHeight(long height) {
        return 1L == height % BATCH_BLOCK_NUM;
    }

    public long getBatchStartHeight(long height) {
        long mod = height % BATCH_BLOCK_NUM;
        if (mod == 1L) {
            return height - 2;
        } else if (mod == 2L) {
            return height;
        } else if (mod == 0L) {
            return height - 1;
        }
        throw new RuntimeException("height is error");
    }

    public long getBatchEndHeight(long height) {
        return getBatchStartHeight(height) + BATCH_BLOCK_NUM - 1L;
    }

    public boolean canPackBlock(long height, String address) {
        long batchStartHeight = getBatchStartHeight(height);
        if (batchStartHeight > height) {
            throw new RuntimeException("the batchStartHeight should not be smaller than the height,the batchStartHeight " + batchStartHeight + ",the height " + height);
        }
        List<String> dposNodes = this.getDposGroupByHeihgt(height);
        if (CollectionUtils.isEmpty(dposNodes)) {
            LOGGER.error("the dpos node is empty with the height {}", height);
            return false;
        }
        if (!dposNodes.contains(address)) {
            LOGGER.info("the address is not in the dpos nodes,the height {},the address {}, the nodes {}", height, address, dposNodes);
            return false;
        }
        return true;
    }

    private long getBeforePreBatchLastHeight(long sn) {
        return Math.max(sn - 3L, 0L) * BATCH_BLOCK_NUM + 1L;
    }

    public long getSn(long height) {
        if (height == 1L) {
            return 1L;
        }
        return (height - DPOS_START_HEIGHT) / BATCH_BLOCK_NUM + 2L;
    }

}
