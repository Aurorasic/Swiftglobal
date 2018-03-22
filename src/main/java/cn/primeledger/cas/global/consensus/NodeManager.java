package cn.primeledger.cas.global.consensus;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.consensus.sign.service.CollectSignService;
import cn.primeledger.cas.global.crypto.ECKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * @author yangyi
 * @deta 2018/3/7
 * @description
 */
@Component
@Slf4j
public class NodeManager {

    @Autowired
    private BlockTimer blockTimer;

    @Autowired
    private ScoreManager scoreManager;

    @Resource(name = "blockData")
    private ConcurrentMap<String, Block> blockMap;

    private List<String> nextGroup = new ArrayList<>();

    private List<String> currentGroup = new ArrayList<>();

    private Map<Integer, Map<String, Integer>> groupMap = new HashMap<>();

    private NodeComparator nodeComparator = new NodeComparator();

    public static final int NODESIZE = 5;

    public boolean parse(Block block) {
        if (block == null) {
            return false;
        }
        long height = block.getHeight();
        List<String> addressList = block.getNodes();
        if (addressList == null || addressList.size() != NODESIZE) {return false;
        }
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
        if ((height - 1) % NodeSelector.BATCHBLOCKNUM == 0) {
            List<String> nodes = null;
            try {
                nodes = processSort(groupMap, nodeComparator);
            } catch (RuntimeException e) {
                LOGGER.error(e.getMessage(), e);
                LOGGER.error("the groupMap is {} \n the nodes is {}\n the scoreMap is {}", groupMap, addressList, scoreManager.getDposMinerSoreMap());
            }

            height = height + CollectSignService.witnessNum;
            LOGGER.info("current block {}_{} selected dpos nodes: {}", height, block.getHash(), nodes);
            blockTimer.processCandidates(height, nodes);
            currentGroup = nextGroup;
            nextGroup = nodes;
            groupMap.clear();
            blockTimer.processBlock(block);
            return true;
        }
//        blockTimer.processRelease(block);
        blockTimer.removeKey(height);
        return false;
    }

    private List<String> processSort(Map<Integer, Map<String, Integer>> groupMap, NodeComparator nodeComparator) {
        List<String> nodes = new ArrayList<>();
        for (int k = 1; k <= groupMap.size(); k++) {
            Map<String, Integer> indexMap = groupMap.get(k);
            if (indexMap == null) {
                throw new RuntimeException("the indexMap is null,the index is " + k);
            }
            nodeComparator.setMap(indexMap);
            Map<String, Integer> sortMap = new TreeMap<>(nodeComparator);
            sortMap.putAll(indexMap);
            Set<Map.Entry<String, Integer>> entries = sortMap.entrySet();
            Map.Entry<String, Integer> next = entries.iterator().next();
            String value = next.getKey();
            nodes.add(value);
        }
        return nodes;
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

    public List<String> getNextGroup() {
        return nextGroup;
    }

    public List<String> getCurrentGroup() {
        return currentGroup;
    }

    public boolean checkProducer(String pubKey) {
        String addr = ECKey.pubKey2Base58Address(pubKey);
        return currentGroup.contains(addr);
    }
}
