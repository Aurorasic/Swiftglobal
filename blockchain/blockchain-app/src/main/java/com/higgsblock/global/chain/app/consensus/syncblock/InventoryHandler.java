package com.higgsblock.global.chain.app.consensus.syncblock;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.higgsblock.global.chain.app.blockchain.BlockCacheManager;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.SystemStatus;
import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Component("inventoryHandler")
@Slf4j
public class InventoryHandler extends BaseEntityHandler<Inventory> {

    private static final long SYNC_BLOCK_EXPIRATION_IN_RUNNING = 1000L;

    @Autowired
    private BlockService blockService;

    @Autowired
    private BlockCacheManager blockCacheManager;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private SystemStatusManager systemStatusManager;

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
        Set<String> peerHashs = data.getHashs();

        peerHashs.forEach(hash -> {
            if (!isExist(hash)) {
                requestRecord.get(hash, v -> {
                    GetData getData = new GetData(height, hash);
                    messageCenter.unicast(sourceId, getData);
                    return height;
                });
            }
        });
    }

    private boolean isExist(String hash) {
        return blockService.isExistInDB(hash) || blockCacheManager.isContains(hash);
    }
}