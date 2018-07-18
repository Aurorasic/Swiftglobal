package com.higgsblock.global.chain.app.sync;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.app.blockchain.BlockProcessor;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SystemStatus;
import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.common.SystemStepEnum;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.common.event.ReceiveOrphanBlockEvent;
import com.higgsblock.global.chain.app.common.event.SystemStatusEvent;
import com.higgsblock.global.chain.app.net.ConnectionManager;
import com.higgsblock.global.chain.app.service.impl.BlockIndexService;
import com.higgsblock.global.chain.app.sync.message.BlockRequest;
import com.higgsblock.global.chain.app.sync.message.Inventory;
import com.higgsblock.global.chain.app.sync.message.MaxHeightRequest;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Component
@Slf4j
public class SyncBlockProcessor implements IEventBusListener, InitializingBean {

    /**
     * the num of active connections to trigger sync data
     */
    private static final int ACTIVE_CONNECTION_NUM = 5;

    private static final int SYNC_BLOCK_TEMP_SIZE = 3;

    private static final int SYNC_BLOCK_EXPIRATION = 10;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private BlockProcessor blockProcessor;

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private SystemStatusManager systemStatusManager;

    @Autowired
    private BlockIndexService blockIndexService;

    private CountDownLatch countDownLatch = new CountDownLatch(ACTIVE_CONNECTION_NUM);

    private ConcurrentHashMap<String, Long> peersMaxHeight = new ConcurrentHashMap<>();

    /**
     * long :block height
     * String :source ID
     */
    private Cache<Long, String> requestRecord = Caffeine.newBuilder().maximumSize(100)
            .expireAfterWrite(SYNC_BLOCK_EXPIRATION, TimeUnit.SECONDS)
            .removalListener((RemovalListener<Long, String>) (height, sourceId, cause) -> {
                if (cause.wasEvicted() && null != height) {
                    dealTimeOut(height, sourceId);
                }
            })
            .build();

    private ScheduledExecutorService scheduledExecutorService = ExecutorServices.newScheduledThreadPool("cache clean up", 1);

    /**
     * 1 represents syncing block in init state
     * 2 represents syncing block in running state
     */
    private int syncState = 1;

    private boolean isSyncBlockState() {
        return syncState == 1;
    }

    public void startSyncBlock() {
        /*
        1.At the beginning of synchronization, ask the nodes you have already connected to about their max height
         */
        messageCenter.broadcast(new MaxHeightRequest());

        try {
            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (peersMaxHeight.size() == 0 || getPeersMaxHeight() <= blockProcessor.getMaxHeight()) {
            systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
            LOGGER.info("there is no need to sync block, sync block finished! peers size :{} my max height : {} , peers' max height:{}"
                    , peersMaxHeight.size(), blockProcessor.getMaxHeight(), getPeersMaxHeight());
            return;
        }

        sendInitRequest();
        LOGGER.info("send init sync block request end!");
    }

    private void sendInitRequest() {
        long initHeight = blockProcessor.getMaxHeight();
        for (long i = 1; i <= SYNC_BLOCK_TEMP_SIZE && i + initHeight <= getPeersMaxHeight(); i++) {
            sendGetBlock(initHeight + i);
        }
    }

    private boolean sendGetBlock(long height) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, Long> entry : peersMaxHeight.entrySet()) {
            if (null == connectionManager.getConnectionByPeerId(entry.getKey())) {
                peersMaxHeight.remove(entry.getKey());
                continue;
            }
            if (entry.getValue() >= height) {
                list.add(entry.getKey());
            }
        }
        if (list.size() == 0) {
            return false;
        }
        String sourceId = list.get(new Random().nextInt(list.size()));
        requestRecord.get(height, v -> {
            messageCenter.unicast(sourceId, new BlockRequest(height));
            LOGGER.info("send block request! height:{} ", height);
            return sourceId;
        });
        return true;
    }

    @Subscribe
    public void process(BlockPersistedEvent event) {
        LOGGER.info("process event: {}", event);
        broadcastInventory(event);
        continueSyncBlock(event);
    }

    private void broadcastInventory(BlockPersistedEvent event) {
        long height = event.getHeight();
        String sourceId = event.getSourceId();
        Inventory inventory = new Inventory();
        inventory.setHeight(height);
        Set<String> set = new HashSet<>(blockIndexService.getBlockIndexByHeight(height).getBlockHashs());
        inventory.setHashs(set);
        messageCenter.broadcast(new String[]{sourceId}, inventory);
    }

    /**
     * check whether to continue sync block
     */
    private void continueSyncBlock(BlockPersistedEvent event) {
        if (getPeersMaxHeight() < event.getHeight()) {
            return;
        }

        //when there has a persisted block on the height, stop sycn this height.If another one is real best block on
        // the height, its next block maybe orphan block, then fetch the real best block as orphan block.
        requestRecord.invalidate(event.getHeight());
        long targetHeight = event.getHeight() + SYNC_BLOCK_TEMP_SIZE;
        if (targetHeight <= getPeersMaxHeight()) {
            sendGetBlock(targetHeight);
        }
        if (isSyncBlockState() && blockProcessor.getMaxHeight() >= getPeersMaxHeight()) {
            systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
            LOGGER.info("sync block finished !");
        }
    }


    @Subscribe
    public void process(SystemStatusEvent event) {
        LOGGER.info("process SystemStatusEvent: {}", event);
        SystemStatus state = event.getSystemStatus();
        if (SystemStatus.RUNNING == state) {
            syncState = 2;
        }
    }

    @Subscribe
    public void process(ReceiveOrphanBlockEvent event) {
        LOGGER.info("process ReceiveOrphanBlockEvent: {}", event);
        long height = event.getHeight();
        String sourceId = event.getSourceId();
        String hash = event.getBlockHash();

        //if peer's connection is lost
        if (null == connectionManager.getConnectionByPeerId(sourceId)) {
            return;
        }

        //update peer's max height
        peersMaxHeight.compute(sourceId, (k, v) -> {
            if (null == v) {
                return height;
            } else {
                return height > v ? height : v;
            }
        });

        if (height <= blockProcessor.getMaxHeight()) {
            requestRecord.get(height, v -> {
                messageCenter.unicast(sourceId, new BlockRequest(height, hash));
                LOGGER.info("send block request! height:{},hash:{} ", height, hash);
                return sourceId;
            });
            return;
        }

        //get all blocks by height
        sendInitRequest();

    }

    private void dealTimeOut(long height, String sourceId) {
        if (height <= blockProcessor.getMaxHeight()) {
            return;
        }
        LOGGER.info("time out, remove it .sourceId:{} ", sourceId);
        removePeer(sourceId);
        if (!sendGetBlock(height)) {
            if (blockProcessor.getMaxHeight() >= getPeersMaxHeight() && isSyncBlockState()) {
                systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
                LOGGER.info("sync block finished !");
            }
        }
    }

    private void removePeer(String sourceId) {
        connectionManager.remove(sourceId);
        peersMaxHeight.remove(sourceId);
    }

    /**
     * The node takes the initiative to ask the adjacent node for the current block height of the other party
     *
     * @param height
     * @param sourceId
     */
    public void updatePeersMaxHeight(long height, String sourceId) {
        if (null != sourceId && countDownLatch.getCount() > 0L) {
            peersMaxHeight.compute(sourceId, (k, v) -> {
                if (null == v) {
                    countDownLatch.countDown();
                    LOGGER.info("peer's max height is  {}, sourceId is {}", height, sourceId);
                    return height;
                } else {
                    return height > v ? height : v;
                }
            });
        }
    }

    private long getPeersMaxHeight() {
        long myHeight = blockProcessor.getMaxHeight();
        long height = myHeight > 1L ? myHeight : 1L;
        for (Map.Entry<String, Long> entry : peersMaxHeight.entrySet()) {
            long tempHeight = entry.getValue();
            if (tempHeight < myHeight) {
                peersMaxHeight.remove(entry.getKey());
                continue;
            }
            if (tempHeight > height) {
                height = tempHeight;
            }
        }
        return height;
    }

    @Override
    public void afterPropertiesSet() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> requestRecord.cleanUp(), 20, SYNC_BLOCK_EXPIRATION / 5, TimeUnit.SECONDS);
    }
}


