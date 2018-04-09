package cn.primeledger.cas.global.schedule;

import cn.primeledger.cas.global.blockchain.BlockIndex;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.blockchain.listener.MessageCenter;
import cn.primeledger.cas.global.consensus.syncblock.Inventory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @date 2018/3/23
 */
@Slf4j
@Component
public class InventoryTask extends BaseTask {

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private BlockService blockService;

    @Override
    protected void task() {
        long height = blockService.getBestMaxHeight();
        Inventory inventory = new Inventory();
        inventory.setHeight(height);
        BlockIndex blockIndex = blockService.getBlockIndexByHeight(height);
        if (blockIndex != null &&
                CollectionUtils.isNotEmpty(blockIndex.getBlockHashs())) {
            Set<String> set = new HashSet<>(blockIndex.getBlockHashs());
            inventory.setHashs(set);
        }
        messageCenter.broadcast(inventory);
    }

    @Override
    protected long getPeriodMs() {
        return TimeUnit.SECONDS.toMillis(3);
    }
}
