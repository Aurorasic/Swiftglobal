package com.higgsblock.global.chain.app.consensus.syncblock;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.OrphanBlockCacheManager;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.SystemStatus;
import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.common.event.ReceiveOrphanBlockEvent;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Component("inventoryHandler")
@Slf4j
public class InventoryHandler extends BaseEntityHandler<Inventory> {

    private static final long SYNC_BLOCK_EXPIRATION_IN_RUNNING = 1000L;

    @Autowired
    private BlockService blockService;

    @Autowired
    private OrphanBlockCacheManager orphanBlockCacheManager;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private SystemStatusManager systemStatusManager;

    @Autowired
    private EventBus eventBus;

    private Cache<String, Long> requestRecord = Caffeine.newBuilder().maximumSize(200)
            .expireAfterWrite(SYNC_BLOCK_EXPIRATION_IN_RUNNING, TimeUnit.MILLISECONDS)
            .build();

    @Override
    protected void process(SocketRequest<Inventory> request) {
        if (!systemStatusManager.getSystemStatus().equals(SystemStatus.RUNNING)) {
            return;
        }
        Inventory data = request.getData();
        String sourceId = request.getSourceId();
        long height = data.getHeight();
        Set<String> hashs = data.getHashs();
        if (height <= blockService.getMaxHeight() + 1L) {
            hashs.forEach(hash -> requestRecord.get(hash, v -> {
                if (!blockService.isExistInDB(height, hash)) {
                    GetBlock getBlock = new GetBlock(height, hash);
                    messageCenter.unicast(sourceId, getBlock);
                    return height;
                }
                return null;
            }));
        } else if (height > blockService.getMaxHeight() + 1L && CollectionUtils.isNotEmpty(hashs)) {
            String hash = new ArrayList<>(hashs).get(0);
            eventBus.post(new ReceiveOrphanBlockEvent(height, hash, sourceId));
        }


    }
}