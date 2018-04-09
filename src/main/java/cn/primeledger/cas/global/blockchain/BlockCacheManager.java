package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.consensus.syncblock.SyncBlockService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * orphan blocks manager storing blocks cannot been connected on chain
 *
 * @author yuguojia
 * @date 2018/2/28
 **/
@Service
@Data
@Slf4j
public class BlockCacheManager {
    private static final int MAX_CACHE_HEIGHT = 16;

    private final Map<Long, Map<String, BlockFullInfo>> orphanBlockMap;
    @Autowired
    private BlockService blockService;
    @Autowired
    private SyncBlockService sycBlockService;

    public BlockCacheManager() {
        Comparator<Long> byHeight = Comparator.comparing(Long::longValue).reversed();
        orphanBlockMap = Collections.synchronizedMap(new TreeMap<>(byHeight));
    }

    public boolean putAndRequestPreBlocks(BlockFullInfo blockInfo) {
        long blockHeight = blockInfo.getBlock().getHeight();
        LOGGER.info("Orphan block cache, map size: {}, height: {}", orphanBlockMap.size(), blockHeight);
        while (orphanBlockMap.size() >= MAX_CACHE_HEIGHT) {
            orphanBlockMap.remove(orphanBlockMap.keySet().iterator().next());
        }

        Map<String, BlockFullInfo> existOrphanBlocks = orphanBlockMap.get(blockHeight);
        if (existOrphanBlocks == null) {
            existOrphanBlocks = new ConcurrentHashMap<>();
            existOrphanBlocks.put(blockInfo.getBlock().getHash(), blockInfo);
            orphanBlockMap.put(blockHeight, existOrphanBlocks);
        } else {
            existOrphanBlocks.put(blockInfo.getBlock().getHash(), blockInfo);
        }

        requestPreBlocks();

        return true;
    }

    public void remove(final String blockHash) {
        for (Map<String, BlockFullInfo> blocks : orphanBlockMap.values()) {
            if (blocks.remove(blockHash) != null) {
                break;
            }
        }
    }

    public boolean isContains(final String blockHash) {
        boolean exist = false;
        for (Map<String, BlockFullInfo> blocks : orphanBlockMap.values()) {
            if (blocks.containsKey(blockHash)) {
                exist = true;
                break;
            }
        }

        return exist;
    }

    public List<BlockFullInfo> getNextConnectionBlocks(String blockHash) {
        List<BlockFullInfo> result = new LinkedList<>();
        if (StringUtils.isEmpty(blockHash)) {
            return result;
        }

        List<BlockFullInfo> blockFullInfos = new LinkedList<>();

        for (Map<String, BlockFullInfo> blocks : orphanBlockMap.values()) {
            blockFullInfos.addAll(blocks.values());
        }
        for (BlockFullInfo blockFullInfo : blockFullInfos) {
            Block block = blockFullInfo.getBlock();
            if (StringUtils.equals(blockHash, block.getPrevBlockHash())) {
                result.add(blockFullInfo);
            }
        }
        return result;
    }

    public void requestPreBlocks() {
        List<BlockFullInfo> blockFullInfos = new LinkedList<>();
        for (Map<String, BlockFullInfo> blocks : orphanBlockMap.values()) {
            blockFullInfos.addAll(blocks.values());
        }

        for (BlockFullInfo blockFullInfo : blockFullInfos) {
            Block block = blockFullInfo.getBlock();
            String prevBlockHash = block.getPrevBlockHash();
            if (StringUtils.isEmpty(prevBlockHash) || isContains(prevBlockHash)) {
//                LOGGER.warn("pre block={} is in the orphan block cache, donot fetch its pre-blocks", prevBlockHash);
                continue;
            }
            long preHeight = block.getHeight() - 1;
            sycBlockService.syncBlockByHeight(preHeight, blockFullInfo.getSourceId());
            LOGGER.info("height={}_block={} is orphan block or no best pre block, fetch pre height blocks", block.getHeight(), block.getHash());
        }
    }

}