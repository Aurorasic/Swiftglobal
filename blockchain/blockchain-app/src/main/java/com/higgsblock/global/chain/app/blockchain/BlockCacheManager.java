package com.higgsblock.global.chain.app.blockchain;

import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.common.event.ReceiveOrphanBlockEvent;
import com.higgsblock.global.chain.app.consensus.syncblock.SyncBlockService;
import com.higgsblock.global.chain.app.utils.ValueSortedMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private static final int MAX_CACHE_SIZE = 50;

    private final Map<String, BlockFullInfo> orphanBlockMap;
    @Autowired
    private BlockService blockService;
    @Autowired
    private SyncBlockService sycBlockService;

    @Autowired
    private EventBus eventBus;

    public BlockCacheManager() {
        Comparator<BlockFullInfo> comparator = new ResultsComparator();
        orphanBlockMap = new ValueSortedMap(comparator);

    }

    public boolean putAndRequestPreBlocks(BlockFullInfo blockInfo) {
        long blockHeight = blockInfo.getBlock().getHeight();
        LOGGER.info("Orphan block cache, map size: {}, height: {}", orphanBlockMap.size(), blockHeight);

        Iterator<String> iterator = orphanBlockMap.keySet().iterator();
        while (orphanBlockMap.size() > MAX_CACHE_SIZE) {
            orphanBlockMap.remove(iterator.next());
        }

        orphanBlockMap.put(blockInfo.getBlock().getHash(), blockInfo);
        requestPreBlocks();

        return true;
    }

    public BlockFullInfo remove(final String blockHash) {
        if (orphanBlockMap != null) {
            return orphanBlockMap.remove(blockHash);
        }
        return null;
    }

    public boolean isContains(final String blockHash) {
        BlockFullInfo blockFullInfo = orphanBlockMap.get(blockHash);
        return blockFullInfo != null;
    }

    public List<BlockFullInfo> getNextConnectionBlocks(String blockHash) {
        List<BlockFullInfo> result = new LinkedList<>();
        if (StringUtils.isEmpty(blockHash)) {
            return result;
        }

        List<BlockFullInfo> blockFullInfos = new LinkedList<>(orphanBlockMap.values());
        for (BlockFullInfo blockFullInfo : blockFullInfos) {
            Block block = blockFullInfo.getBlock();
            if (StringUtils.equals(blockHash, block.getPrevBlockHash())) {
                result.add(blockFullInfo);
            }
        }
        return result;
    }

    public void requestPreBlocks() {
        List<BlockFullInfo> blockFullInfos = new LinkedList<>(orphanBlockMap.values());
        for (BlockFullInfo blockFullInfo : blockFullInfos) {
            Block block = blockFullInfo.getBlock();
            String prevBlockHash = block.getPrevBlockHash();
            if (StringUtils.isEmpty(prevBlockHash) || isContains(prevBlockHash)) {
//                LOGGER.warn("pre block={} is in the orphan block cache, donot fetch its pre-blocks", prevBlockHash);
                continue;
            }
            long preHeight = block.getHeight() - 1;
//            sycBlockService.syncBlockByHeight(preHeight, blockFullInfo.getSourceId());
            eventBus.post(new ReceiveOrphanBlockEvent(block.getHeight(), block.getHash(), blockFullInfo.getSourceId()));
            LOGGER.info("height={}_block={} is orphan block or no best pre block, fetch pre height blocks", block.getHeight(), block.getHash());
        }
    }

    private static class ResultsComparator implements Comparator<BlockFullInfo> {
        @Override
        public int compare(BlockFullInfo t, BlockFullInfo t1) {
            if (t.getBlock().getHeight() > t1.getBlock().getHeight()) {
                return -1;
            } else if (t.getBlock().getHeight() == t1.getBlock().getHeight()) {
                return 0;
            } else {
                return 1;
            }
        }
    }

}