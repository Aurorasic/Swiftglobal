package cn.primeledger.cas.global.consensus;

import cn.primeledger.cas.global.blockchain.Block;
import cn.primeledger.cas.global.blockchain.BlockService;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yangyi
 * @deta 2018/3/6
 * @description
 */
@Service
@Slf4j
public class NodeSelector {

    /**
     * the number of the block each group should pack
     */
    public static final int BATCHBLOCKNUM = 3;
    private final int maxScore = 1000;
    private final int midScore = 800;
    private final int mixScore = 600;
    @Autowired
    private ScoreManager scoreManager;
    @Autowired
    private BlockService blockService;
    @Autowired
    private NodeManager nodeManager;

    public List<String> calculateNodes(long lastBlockHeight) {

        Map<String, Integer> minerSoreMap = scoreManager.getDposMinerSoreMap();
        if (minerSoreMap.size() < NodeManager.NODESIZE) {
            return ListUtils.EMPTY_LIST;
        }

        long height = (lastBlockHeight - 1) / BATCHBLOCKNUM * BATCHBLOCKNUM + 1;
        if (height < 7) {
            return ListUtils.EMPTY_LIST;
        }
        Block block = blockService.getBestBlockByHeight(height);
        final String hash = block.getHash();
        LOGGER.info("begin to select dpos node when pack block,and the hash of the last block in the previous batch is {}", hash);
        LOGGER.info("the minerSoreMap to select dpos is {}", minerSoreMap);

        final List<String> currentGroup = nodeManager.getDposGroup(lastBlockHeight + 1);
        final List<String> nextGroup = nodeManager.getNextGroup(lastBlockHeight + 1);
        LOGGER.info("the currentGroup is {}", currentGroup);
        LOGGER.info("the nextGroup is {}", nextGroup);

        List<String> maxScoreList = new ArrayList<>();
        List<String> midScoreList = new ArrayList<>();
        List<String> minScoreList = new ArrayList<>();
        List<String> inadequateScoreList = new ArrayList<>();
        minerSoreMap.forEach((address, score) -> {
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
        if (select.size() < 3) {
            throw new RuntimeException("can not find enough dpos node");
        }
        return select;
    }

}
