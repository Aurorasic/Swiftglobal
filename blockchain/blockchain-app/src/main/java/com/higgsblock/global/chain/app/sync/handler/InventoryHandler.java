package com.higgsblock.global.chain.app.sync.handler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.event.SyncBlockEvent;
import com.higgsblock.global.chain.app.common.handler.BaseMessageHandler;
import com.higgsblock.global.chain.app.sync.message.BlockRequest;
import com.higgsblock.global.chain.app.sync.message.Inventory;
import com.higgsblock.global.chain.network.socket.message.IMessage;
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
    protected boolean valid(IMessage<Inventory> message) {
        Inventory data = message.getData();
        return null != data && data.valid();
    }

    @Override
    protected void process(IMessage<Inventory> message) {
        long processStartTime = System.currentTimeMillis();
        Inventory data = message.getData();
        String sourceId = message.getSourceId();
        long height = data.getHeight();
        Set<String> hashes = data.getHashs();
        if (height <= blockChainService.getMaxHeight() + 1L) {
            hashes.forEach(hash -> requestRecord.get(hash, v -> {
                if (!blockChainService.isExistBlock(hash)) {
                    BlockRequest blockRequest = new BlockRequest(height, hash);
                    messageCenter.unicast(sourceId, blockRequest);
                    
                    long processEndTime = System.currentTimeMillis();
                    LOGGER.info("send inventory info spend time :{}ms", processEndTime - processStartTime);
                    return height;
                }

                long processEndTime1 = System.currentTimeMillis();
                LOGGER.info("check hash exist db spend time :{}ms", processEndTime1 - processStartTime);
                return null;
            }));
        } else if (height > blockChainService.getMaxHeight() + 1L && CollectionUtils.isNotEmpty(hashes)) {
            eventBus.post(new SyncBlockEvent(height, null, sourceId));
        }

        long processEndTime2 = System.currentTimeMillis();
        LOGGER.info("send sync block event spend time :{}ms", processEndTime2 - processStartTime);
    }
}