package com.higgsblock.global.chain.app.consensus;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.higgsblock.global.chain.app.Application;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.blockchain.BlockIndex;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.BlockWitness;
import com.higgsblock.global.chain.app.consensus.sign.service.CollectSignService;
import com.higgsblock.global.chain.crypto.ECKey;
import com.higgsblock.global.chain.crypto.KeyPair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

/**
 * @author yangyi
 * @deta 2018/3/7
 * @description
 */
@Component
@Slf4j
public class NodeManager implements InitializingBean {

    public static final int NODESIZE = 5;
    private static final int MAX_SIZE = 20;
    @Autowired
    private BlockService blockService;
    @Autowired
    private ScoreManager scoreManager;
    @Autowired
    private KeyPair keyPair;
    private String myaddr = ECKey.pubKey2Base58Address(keyPair);
    private Cache<Long, List<String>> dposNodeMap = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .build();
    private Map<Integer, Map<String, Integer>> groupMap = new HashMap<>();
    private NodeComparator nodeComparator = new NodeComparator();
    private Function<Long, List<String>> function = null;

    public boolean parseDpos(Block block) {
        if (block == null) {
            return false;
        }
        long height = block.getHeight();
        List<String> addressList = block.getNodes();
        if (addressList == null || addressList.size() != NODESIZE) {
            return false;
        }
        BlockIndex blockIndexByHeight = blockService.getBlockIndexByHeight(height);
        LOGGER.info("the blockIndex is {}", blockIndexByHeight);
        LOGGER.info("the height {} the nodes are {}  block {}", height, addressList, block);
        List<String> currentGroup = getDposGroup(height);
        List<String> nextGroup = getNextGroup(height);
        for (int i = 0; i < addressList.size(); i++) {
            String address = addressList.get(i);
            if (currentGroup.contains(address) || nextGroup.contains(address)) {
                continue;
            }
            Map<String, Integer> indexMap = groupMap.get(i + 1);
            if (indexMap == null) {
                indexMap = new HashMap<>();
                groupMap.put(i + 1, indexMap);
            }
            Integer time = indexMap.get(address);
            if (time == null) {
                indexMap.put(address, Integer.valueOf(1));
            } else {
                indexMap.put(address, time + 1);
            }
        }
        if (isEndHeight(height)) {
            List<String> nodes = null;
            try {
                nodes = processSort(groupMap, nodeComparator);
            } catch (RuntimeException e) {
                LOGGER.error(e.getMessage(), e);
                LOGGER.error("the groupMap is {} \n the nodes is {}\n the scoreMap is {}", groupMap, addressList, scoreManager.getDposMinerSoreMap());
            }

            height = height + CollectSignService.witnessNum;
            LOGGER.info("current block {}_{} selected dpos nodes: {}", height, block.getHash(), nodes);
            dposNodeMap.put(height, nodes);
            groupMap.clear();
            return true;
        }
        return false;
    }

    private List<String> processSort(Map<Integer, Map<String, Integer>> groupMap, NodeComparator nodeComparator) {
        List<String> nodes = Lists.newLinkedList();
        for (int k = 1; k <= groupMap.size(); k++) {
            Map<String, Integer> indexMap = groupMap.get(k);
            if (indexMap == null) {
                throw new RuntimeException("the indexMap is null,the index is " + k);
            }
            nodeComparator.setMap(indexMap);
            Map<String, Integer> sortMap = new TreeMap<>(nodeComparator);
            sortMap.putAll(indexMap);
            Set<Map.Entry<String, Integer>> entries = sortMap.entrySet();
            Iterator<Map.Entry<String, Integer>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> next = iterator.next();
                String value = next.getKey();
                if (!nodes.contains(value)) {
                    nodes.add(value);
                    break;
                }
            }
        }
        return nodes;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        function = (height) -> {
            List<String> result = Lists.newLinkedList();
            long endHeight = height - NodeSelector.BATCHBLOCKNUM;
            List<Block> blocks = blockService.getBestBatchBlocks(endHeight);
            if (blocks.size() != NodeSelector.BATCHBLOCKNUM) {
                return result;
            }
            Map<Integer, Map<String, Integer>> groupMap = new HashMap<>();
            Iterator<Block> iterator = blocks.iterator();
            while (iterator.hasNext()) {
                Block block = iterator.next();
                List<String> addressList = block.getNodes();
                if (addressList == null || addressList.size() != NODESIZE) {
                    return result;
                }
                for (int i = 0; i < addressList.size(); i++) {
                    String address = addressList.get(i);
                    Map<String, Integer> indexMap = groupMap.get(i + 1);
                    if (indexMap == null) {
                        indexMap = new HashMap<>();
                        groupMap.put(i + 1, indexMap);
                    }
                    Integer time = indexMap.get(address);
                    if (time == null) {
                        indexMap.put(address, Integer.valueOf(1));
                    } else {
                        indexMap.put(address, time + 1);
                    }
                }
            }
            try {
                result = processSort(groupMap, nodeComparator);
                LOGGER.info(" selected dpos nodes: {}", result);
                return result;
            } catch (RuntimeException e) {
                LOGGER.error(e.getMessage(), e);
            }
            return result;
        };
    }

    public List<String> getNextGroup(long height) {
        long batchEndHeight = getBatchEndHeight(height);
        return dposNodeMap.get(batchEndHeight, function);
    }

    public List<String> getDposGroup(long height) {
        long batchEndHeight = getBatchEndHeight(height);
        batchEndHeight = batchEndHeight - NodeSelector.BATCHBLOCKNUM;
        return dposNodeMap.get(batchEndHeight, function);
    }

    public boolean checkProducer(Block block) {
        BlockWitness blockWitness = block.getMinerFirstPKSig();
        String address = ECKey.pubKey2Base58Address(blockWitness.getPubKey());
        List<String> currentGroup = this.getDposGroup(block.getHeight());
        return CollectionUtils.isNotEmpty(currentGroup) && currentGroup.contains(address);
    }

    public int getFullBlockCountByHeight(long height) {
        long mod = (height - Application.PRE_BLOCK_COUNT) % NodeSelector.BATCHBLOCKNUM;
        if (mod == 1) {
            return NodeManager.NODESIZE;
        } else if (mod == 2) {
            return NodeManager.NODESIZE - 1;
        } else if (mod == 0) {
            return NodeManager.NODESIZE - 2;
        }
        throw new RuntimeException("height is error");
    }

    public boolean isEndHeight(long height) {
        return 0L == (height - Application.PRE_BLOCK_COUNT + 2 * NodeSelector.BATCHBLOCKNUM) % NodeSelector.BATCHBLOCKNUM;
    }

    public long getBatchStartHeight(long height) {
        long mod = (height - Application.PRE_BLOCK_COUNT + 2 * NodeSelector.BATCHBLOCKNUM) % NodeSelector.BATCHBLOCKNUM;
        if (mod == 1) {
            return height;
        } else if (mod == 2) {
            return height - 1;
        } else if (mod == 0) {
            return height - 2;
        }
        throw new RuntimeException("height is error");
    }

    public long getBatchEndHeight(long height) {
        long mod = (height - Application.PRE_BLOCK_COUNT + 2 * NodeSelector.BATCHBLOCKNUM) % NodeSelector.BATCHBLOCKNUM;
        if (mod == 1) {
            return height + 2;
        } else if (mod == 2) {
            return height + 1;
        } else if (mod == 0) {
            return height;
        }
        throw new RuntimeException("height is error");
    }

    public boolean canPackBlock(long height, String address) {
        long batchStartHeight = getBatchStartHeight(height);
        if (batchStartHeight > height) {
            throw new RuntimeException("the batchStartHeight should not be smaller than the height,the batchStartHeight " + batchStartHeight + ",the height " + height);
        }
        Block block = null;
        while (batchStartHeight < height && (block = blockService.getBestBlockByHeight(batchStartHeight)) != null) {
            if (block == null) {
                LOGGER.error("can not find best block by height {}", batchStartHeight);
                return false;
            }
            BlockWitness minerFirstPKSig = block.getMinerFirstPKSig();
            if (minerFirstPKSig == null) {
                LOGGER.error("can not find minerFirstPKSig in block {}", block);
                return false;
            }
            String minerAddress = minerFirstPKSig.getAddress();
            if (StringUtils.isEmpty(minerAddress)) {
                LOGGER.error("the miner address is empty {} ", block);
                return false;
            }
            if (StringUtils.equals(address, minerAddress)) {
                LOGGER.info("can not pack the block with height {},address {} ", height, address);
                return false;
            }
            batchStartHeight++;
        }
        List<String> dposNodes = this.getDposGroup(height);
        if (CollectionUtils.isEmpty(dposNodes)) {
            LOGGER.error("the dpos node is empty with the height {}", height);
            return false;
        }
        if (!dposNodes.contains(address)) {
            LOGGER.info("the address is not in the dpos nodes,the height {},the address {}, the nodes {}", height, address, dposNodes);
            return false;
        }
        List<Block> blocks = blockService.getBlocksByHeight(height);
        if (CollectionUtils.isEmpty(blocks)) {
            return true;
        }
        for (Block temp : blocks) {
            BlockWitness minerFirstPKSig = temp.getMinerFirstPKSig();
            if (minerFirstPKSig == null) {
                LOGGER.error("can not find minerFirstPKSig in temp {}", block);
                return false;
            }
            String minerAddress = minerFirstPKSig.getAddress();
            if (StringUtils.isEmpty(minerAddress)) {
                LOGGER.error("the miner address is empty {} ", block);
                return false;
            }
            if (StringUtils.equals(minerAddress, address)) {
                return false;
            }
        }
        return true;
    }

    public boolean canPackBlock(long height) {
        return canPackBlock(height, myaddr);
    }

    static class NodeComparator implements Comparator<String> {

        private Map<String, Integer> map;

        public void setMap(Map<String, Integer> map) {
            this.map = map;
        }

        @Override
        public int compare(String o1, String o2) {
            Integer integer = map.get(o1);
            Integer integer1 = map.get(o2);
            int i = integer.compareTo(integer1);
            if (i != 0) {
                return i * -1;
            }
            return -1;
        }
    }

}
