package com.higgsblock.global.chain.app.blockchain;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.common.event.ReceiveOrphanBlockEvent;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import com.higgsblock.global.chain.app.sync.SyncBlockProcessor;
import com.higgsblock.global.chain.app.utils.ValueSortedMap;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
public class OrphanBlockCacheManager implements IEventBusListener {
    private static final int MAX_CACHE_SIZE = 50;

    private final Map<String, BlockFullInfo> orphanBlockMap;
    @Autowired
    private BlockProcessor blockProcessor;
    @Autowired
    private BlockService blockService;
    @Autowired
    private SyncBlockProcessor sycBlockService;

    @Autowired
    private EventBus eventBus;

    public OrphanBlockCacheManager() {
        Comparator<BlockFullInfo> comparator = new ResultsComparator();
        orphanBlockMap = new ValueSortedMap(comparator);

    }

    public boolean putAndRequestPreBlocks(BlockFullInfo blockInfo) {
        putPreBlocks(blockInfo);
        requestPreBlocks();
        return true;
    }

    public void putPreBlocks(BlockFullInfo blockInfo) {
        long blockHeight = blockInfo.getBlock().getHeight();
        LOGGER.info("Orphan block cache, map size: {}, height: {}", orphanBlockMap.size(), blockHeight);

        Iterator<String> iterator = orphanBlockMap.keySet().iterator();
        while (orphanBlockMap.size() > MAX_CACHE_SIZE) {
            orphanBlockMap.remove(iterator.next());
        }
        orphanBlockMap.put(blockInfo.getBlock().getHash(), blockInfo);
    }

    @Subscribe
    public void process(BlockPersistedEvent event) {
        long height = event.getHeight();
        String blockHash = event.getBlockHash();
        List<BlockFullInfo> nextConnectionBlocks = getNextConnectionBlocks(blockHash);
        if (CollectionUtils.isNotEmpty(nextConnectionBlocks)) {
            for (BlockFullInfo nextBlockFullInfo : nextConnectionBlocks) {
                Block nextBlock = nextBlockFullInfo.getBlock();
                long nextHeight = nextBlock.getHeight();
                String nextBlockHash = nextBlock.getHash();
                String nextSourceId = nextBlockFullInfo.getSourceId();
                int nextVersion = nextBlockFullInfo.getVersion();
                LOGGER.info("persisted height={},block={}, find orphan next block height={},block={} to persist",
                        height, blockHash, nextHeight, nextBlockHash);
                if (!blockProcessor.validBasic(nextBlock)) {
                    LOGGER.error("Error next orphan block height={},block={}", nextHeight, nextBlockHash);
                    remove(nextBlockHash);
                    continue;
                }
                if (!blockProcessor.validBlockTransactions(nextBlock)) {
                    LOGGER.error("Error orphan next block height={},block={}", nextHeight, nextBlockHash);
                    remove(nextBlockHash);
                    continue;
                }
                boolean success = blockService.persistBlockAndIndex(nextBlock, nextSourceId, nextVersion);
                LOGGER.info("orphan manager persisted block all info, success={},height={},block={}",
                        success, nextHeight, nextBlockHash);
            }
        }
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
                continue;
            }
            // get pre block
            eventBus.post(new ReceiveOrphanBlockEvent(block.getHeight() - 1L, block.getPrevBlockHash(), blockFullInfo.getSourceId()));
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