package com.higgsblock.global.chain.app.blockchain;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.common.event.SyncBlockEvent;
import com.higgsblock.global.chain.app.service.impl.BlockService;
import com.higgsblock.global.chain.app.sync.SyncBlockInStartupService;
import com.higgsblock.global.chain.app.sync.message.BlockResponse;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private SyncBlockInStartupService sycBlockService;
    @Autowired
    private MessageCenter messageCenter;
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

        orphanBlockMap.add(blockChainService.getMaxHeight(), blockInfo);
    }

    @Subscribe
    public void process(BlockPersistedEvent event) {
        LOGGER.info("orphanBlockManager received process event: {}", event);
        long height = event.getHeight();
        String blockHash = event.getBlockHash();
        try {
            process(blockHash, height);
        } catch (Exception e) {
            LOGGER.error(String.format("exception when handle orphan blocks of %s", event), e);
        }
    }

    private void process(String blockHash, long height) {
        remove(blockHash);

        List<BlockFullInfo> nextConnectionBlocks = getNextConnectionBlocks(blockHash);
        if (CollectionUtils.isNotEmpty(nextConnectionBlocks)) {
            List<Block> list = new ArrayList<>();
            for (BlockFullInfo nextBlockFullInfo : nextConnectionBlocks) {
                Block nextBlock = nextBlockFullInfo.getBlock();
                String nextBlockHash = nextBlock.getHash();
                remove(nextBlockHash);
                list.add(nextBlock);
            }
            messageCenter.dispatch(new BlockResponse(height + 1L, list));
            LOGGER.info("persisted height={},block={}, found next orphan blocks ,size={} ", height, blockHash, list.size());

        }
    }

    public BlockFullInfo remove(final String blockHash) {
        return orphanBlockMap.remove(blockHash);
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