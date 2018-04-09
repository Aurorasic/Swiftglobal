package cn.primeledger.cas.global.consensus.syncblock;

import cn.primeledger.cas.global.blockchain.BlockIndex;
import cn.primeledger.cas.global.blockchain.BlockService;
import cn.primeledger.cas.global.blockchain.listener.MessageCenter;
import cn.primeledger.cas.global.common.SystemStatus;
import cn.primeledger.cas.global.common.SystemStatusManager;
import cn.primeledger.cas.global.common.SystemStepEnum;
import cn.primeledger.cas.global.common.event.BlockPersistedEvent;
import cn.primeledger.cas.global.common.listener.IEventBusListener;
import cn.primeledger.cas.global.network.socket.connection.ConnectionManager;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Component
@Slf4j
public class SyncBlockService implements IEventBusListener {

    /**
     * the num of active connections to trigger sync data
     */
    private static final int ACTIVE_CONNECTION_NUM = 8;

    private static final int MAX_RETRY_TIMES = 3;

    private static final int SYNC_BLOCK_TEMP_SIZE = 10;

    private static final int SYNC_BLOCK_EXPIRATION = 3;


    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private BlockService blockService;

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private SystemStatusManager systemStatusManager;

    private CountDownLatch countDownLatch = new CountDownLatch(ACTIVE_CONNECTION_NUM);

    private ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, AtomicLong> retryTimes = new ConcurrentHashMap<>();

    private boolean allowUpdatePeersMaxHeight = true;

    private long lastSyncHeight = 0L;

    private Cache<Long, String> cache = Caffeine.newBuilder().maximumSize(20)
            .expireAfterWrite(SYNC_BLOCK_EXPIRATION, TimeUnit.SECONDS)
            .removalListener((RemovalListener<Long, String>) (height, sourceId, cause) -> {
                if (cause.wasEvicted() && null != height) {
                    retrySendSyncBlockRequest(height, sourceId);
                }
            })
            .build();

    public void startSyncBlock() {
        try {
            countDownLatch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (map.size() == 0) {
            systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
            LOGGER.info("have no peer to sync block, sync block finished!");
            return;
        }

        allowUpdatePeersMaxHeight = false;
        long peersMaxHeight = getPeersMaxHeight();
        long startHeight = blockService.getBestMaxHeight();

        if (peersMaxHeight <= startHeight) {
            systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
            LOGGER.info("all peers' max height aren't higher than mine, sync block finished!");
            return;
        }
        sendSyncBlockRequest(startHeight, peersMaxHeight);
        LOGGER.info("send sync block request end!");
    }

    @Subscribe
    public void process(BlockPersistedEvent event) {
        if (systemStatusManager.getSystemStatus().equals(SystemStatus.RUNNING)) {
            return;
        }
        if (event.hasBestBlock()) {
            cache.invalidate(event.getHeight());
            if (event.getHeight() >= lastSyncHeight && systemStatusManager.getSystemStatus().equals(SystemStatus.SYNC_BLOCKS)) {
                systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
                LOGGER.info("sync block finished !");
            }
        }
    }

    /**
     * sync block only called one times in the start process
     *
     * @param startHeight
     * @param endHeight
     */
    private void sendSyncBlockRequest(long startHeight, long endHeight) {
        long syncHeight = startHeight;
        if (syncHeight < 1L) {
            syncHeight = 1L;
        }
        while (syncHeight <= endHeight) {
            if (syncHeight > blockService.getBestMaxHeight() + SYNC_BLOCK_TEMP_SIZE) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                continue;
            }
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                if (entry.getValue() >= syncHeight) {
                    messageCenter.unicast(entry.getKey(), createInventoryByHeight(syncHeight));
                    cache.put(syncHeight, entry.getKey());
                    LOGGER.info("send sync block request height:{} sourceId:{}", syncHeight, entry.getKey());
                    lastSyncHeight = syncHeight;
                    syncHeight++;
                }
                if (syncHeight > endHeight) {
                    break;
                }
            }
        }
    }

    public void retrySendSyncBlockRequest(long syncHeight, String preSourceId) {
        if (syncHeight == blockService.getBestMaxHeight() + 1) {
            retryTimes.computeIfAbsent(preSourceId, s -> new AtomicLong()).incrementAndGet();
            checkTimeoutAndSystemStatus(preSourceId);
            List<String> list = new ArrayList<>();
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                if (!entry.getKey().equals(preSourceId)
                        && entry.getValue() >= syncHeight) {
                    list.add(entry.getKey());
                }
            }

            String sourceId = null;
            if (map.containsKey(preSourceId)) {
                sourceId = preSourceId;
            }
            if (list.size() > 0) {
                sourceId = list.get((int) (Math.random() * (list.size() - 1)));
            }
            if (null == sourceId) {
                return;
            }

            while (true) {
                if (cache.estimatedSize() < SYNC_BLOCK_TEMP_SIZE) {
                    messageCenter.unicast(sourceId, createInventoryByHeight(syncHeight));
                    cache.put(syncHeight, sourceId);
                    break;
                }
                try {
                    Thread.sleep(SYNC_BLOCK_EXPIRATION);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } else {
            cache.put(syncHeight, preSourceId);
        }


    }

    private void checkTimeoutAndSystemStatus(String sourceId) {
        if (retryTimes.get(sourceId).get() > MAX_RETRY_TIMES) {
            connectionManager.close(sourceId);
            map.remove(sourceId);
            lastSyncHeight = getPeersMaxHeight();
            if (map.size() == 0) {
                systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
                LOGGER.info("have no peer to sync block, sync block finished!");
            }
            if (blockService.getBestMaxHeight() >= lastSyncHeight) {
                systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
                LOGGER.info("sync block finished !");
            }
        }
    }

    public void syncBlockByHeight(long targetHeight, String sourceId) {

        /**
         * sync block when SystemStatus is RUNNING
         */
        if (systemStatusManager.getSystemStatus().equals(SystemStatus.RUNNING)) {
            long myMaxHeight = blockService.getBestMaxHeight();
            messageCenter.unicast(sourceId, createInventoryByHeight(myMaxHeight));
            for (long i = myMaxHeight + 1; i <= targetHeight; i++) {
                Inventory inventory = new Inventory();
                inventory.setHeight(i);
                messageCenter.unicast(sourceId, inventory);
            }
        }
    }

    public void updatePeersMaxHeight(long height, String sourceId) {
        if (allowUpdatePeersMaxHeight) {
            if (!map.containsKey(sourceId)) {
                map.put(sourceId, height);
                countDownLatch.countDown();
                LOGGER.info("update peer's max height! maxHeight:{},sourceId:{}", height, sourceId);
            } else if (map.containsKey(sourceId) && height > map.get(sourceId)) {
                map.put(sourceId, height);
            }
        }
    }

    /**
     * get my peers max height
     *
     * @return
     */
    private long getPeersMaxHeight() {
        long height = 1L;
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            long tempHeight = entry.getValue();
            if (tempHeight > height) {
                height = tempHeight;
            }
        }
        return height;
    }

    private Inventory createInventoryByHeight(long height) {
        Inventory inventory = new Inventory();
        inventory.setHeight(height);
        BlockIndex blockIndex = blockService.getBlockIndexByHeight(height);
        if (blockIndex != null &&
                CollectionUtils.isNotEmpty(blockIndex.getBlockHashs())) {
            Set<String> set = new HashSet<>(blockIndex.getBlockHashs());
            inventory.setHashs(set);
        }
        return inventory;
    }
}

