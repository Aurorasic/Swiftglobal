package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.consensus.syncblock.Inventory;
import cn.primeledger.cas.global.consensus.syncblock.SyncBlockService;
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
public class BlockCacheManager {
    // TODO: yuguojia add overTime, and optimaze the syn proccessing of orphan block
    private Map<String, BlockFullInfo> orphanBlockMap = new HashMap(16);

    @Autowired
    private BlockService blockService;
    @Autowired
    private SyncBlockService sycBlockService;

    public boolean put(BlockFullInfo blockInfo) {
        //replace old full block directly
        orphanBlockMap.put(blockInfo.getBlock().getHash(), blockInfo);
        return true;
    }

    public BlockFullInfo remove(String blockHash) {
        if (orphanBlockMap != null) {
            return orphanBlockMap.remove(blockHash);
        }
        return null;
    }

    public boolean isContains(String blockHash) {
        BlockFullInfo blockFullInfo = orphanBlockMap.get(blockHash);
        if (blockFullInfo != null) {
            return true;
        }
        return false;
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

    public void fetchPreBlocks() {
        List<BlockFullInfo> blockFullInfos = new LinkedList<>(orphanBlockMap.values());
        for (BlockFullInfo blockFullInfo : blockFullInfos) {
            Block block = blockFullInfo.getBlock();
            String prevBlockHash = block.getPrevBlockHash();
            if (isContains(prevBlockHash)) {
                //pre block is in the orphan block cache, donot fetch its pre-blocks
                continue;
            }
            long preHeight = block.getHeight() - 1;

            Inventory inventory = new Inventory();
            inventory.setHeight(preHeight);
            BlockIndex preBlockIndex = blockService.getBlockIndexByHeight(preHeight);
            if (preBlockIndex != null &&
                    CollectionUtils.isNotEmpty(preBlockIndex.getBlockHashs())) {
                Set set = new HashSet(preBlockIndex.getBlockHashs());
                inventory.setHashs(set);
            }
            sycBlockService.unicastInventory(inventory, blockFullInfo.getSourceId());
            LOGGER.info("fetch orphan block pre block height: " + preHeight);
        }
    }
}