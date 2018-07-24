package com.higgsblock.global.chain.app.blockchain;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.common.event.SyncBlockEvent;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import com.higgsblock.global.chain.app.sync.SyncBlockService;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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

    private final OrphanBlockCache orphanBlockMap;
    @Autowired
    private IBlockChainService blockChainService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private SyncBlockService sycBlockService;

    @Autowired
    private EventBus eventBus;

    public OrphanBlockCacheManager() {
        orphanBlockMap = new OrphanBlockCache(50);

    }

    public boolean putAndRequestPreBlocks(BlockFullInfo blockInfo) {
        putPreBlocks(blockInfo);
        requestPreBlocks();
        return true;
    }

    public void putPreBlocks(BlockFullInfo blockInfo) {
        Block block = blockInfo.getBlock();
        long blockHeight = block.getHeight();
        LOGGER.info("Orphan block cache, map size: {}, height: {}", orphanBlockMap.size(), blockHeight);

        orphanBlockMap.add(blockInfo);
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
                LOGGER.info("persisted height={},block={}, find orphan next block height={},block={} to persist",
                        height, blockHash, nextHeight, nextBlockHash);

                //check: transactions
                boolean validTransactions = blockChainService.checkTransactions(nextBlock);
                if (!validTransactions) {
                    LOGGER.error("the orphan block transactions are error: {}", nextBlock.getSimpleInfo());
                    remove(nextBlockHash);
                }

                boolean success = blockService.persistBlockAndIndex(nextBlock);
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
            eventBus.post(new SyncBlockEvent(block.getHeight() - 1L, block.getPrevBlockHash(), blockFullInfo.getSourceId()));
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