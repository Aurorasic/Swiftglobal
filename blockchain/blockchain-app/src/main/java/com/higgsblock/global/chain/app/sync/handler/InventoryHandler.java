package com.higgsblock.global.chain.app.sync.handler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.event.ReceiveOrphanBlockEvent;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.sync.message.BlockRequest;
import com.higgsblock.global.chain.app.sync.message.Inventory;
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
    private IBlockChainService blockChainService;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private EventBus eventBus;

    private Cache<String, Long> requestRecord = Caffeine.newBuilder().maximumSize(200)
            .expireAfterWrite(SYNC_BLOCK_EXPIRATION_IN_RUNNING, TimeUnit.MILLISECONDS)
            .build();

    @Override
    protected boolean check(SocketRequest<Inventory> request) {
        Inventory data = request.getData();
        return null != data && data.valid();
    }

    @Override
    protected void process(SocketRequest<Inventory> request) {
        Inventory data = request.getData();
        String sourceId = request.getSourceId();
        long height = data.getHeight();
        Set<String> hashs = data.getHashs();
        if (height <= blockChainService.getMaxHeight() + 1L) {
            hashs.forEach(hash -> requestRecord.get(hash, v -> {
                if (!blockChainService.isExistBlock(hash)) {
                    BlockRequest blockRequest = new BlockRequest(height, hash);
                    messageCenter.unicast(sourceId, blockRequest);
                    return height;
                }
                return null;
            }));
        } else if (height > blockChainService.getMaxHeight() + 1L && CollectionUtils.isNotEmpty(hashs)) {
            eventBus.post(new ReceiveOrphanBlockEvent(height, null, sourceId));
        }
    }
}