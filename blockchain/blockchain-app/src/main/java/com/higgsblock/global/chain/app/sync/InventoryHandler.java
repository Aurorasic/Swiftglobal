package com.higgsblock.global.chain.app.sync;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.SystemStatus;
import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.common.event.ReceiveOrphanBlockEvent;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author yuanjiantao
 * @date 3/8/2018
 */
@Component
@Slf4j
public class InventoryHandler extends BaseMessageHandler<Inventory> {

    private static final long SYNC_BLOCK_EXPIRATION_IN_RUNNING = 1000L;

    @Autowired
    private BlockProcessor blockProcessor;

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
        if (height <= blockProcessor.getMaxHeight() + 1L) {
            hashs.forEach(hash -> requestRecord.get(hash, v -> {
                if (!blockProcessor.isExistInDB(height, hash)) {
                    BlockRequest blockRequest = new BlockRequest(height, hash);
                    messageCenter.unicast(sourceId, blockRequest);
                    return height;
                }
                return null;
            }));
        } else if (height > blockProcessor.getMaxHeight() + 1L && CollectionUtils.isNotEmpty(hashs)) {
            eventBus.post(new ReceiveOrphanBlockEvent(height, null, sourceId));
        }


    }
}