package com.higgsblock.global.chain.app.consensus.syncblock;

import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.app.blockchain.BlockService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SystemStatus;
import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.common.SystemStepEnum;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.common.event.ReceiveOrphanBlockEvent;
import com.higgsblock.global.chain.app.common.event.SystemStatusEvent;
import com.higgsblock.global.chain.app.connection.ConnectionManager;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
public class SyncBlockService implements IEventBusListener, InitializingBean {

    /**
     * the num of active connections to trigger sync data
     */
    private static final int ACTIVE_CONNECTION_NUM = 5;

    private static final int SYNC_BLOCK_TEMP_SIZE = 3;

    private static final int SYNC_BLOCK_EXPIRATION = 10;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private BlockService blockService;

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private SystemStatusManager systemStatusManager;

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

    private boolean isRunningState() {
        return syncState == 2;
    }

    public void startSyncBlock() {
        messageCenter.broadcast(new GetMaxHeight());

        try {
            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (peersMaxHeight.size() == 0 || getPeersMaxHeight() <= blockService.getMaxHeight()) {
            systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
            LOGGER.info("there is no need to sync block, sync block finished! peers size :{} my max height : {} , peers' max height:{}"
                    , peersMaxHeight.size(), blockService.getMaxHeight(), getPeersMaxHeight());
            return;
        }

        sendInitRequest();
        LOGGER.info("send init sync block request end!");
    }

    private void sendInitRequest() {
        long initHeight = blockService.getMaxHeight();
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
            messageCenter.unicast(sourceId, new GetBlock(height));
            LOGGER.info("send block request! height:{} ", height);
            return sourceId;
        });
        return true;
    }

    @Subscribe
    public void process(BlockPersistedEvent event) {
        if (getPeersMaxHeight() < event.getHeight()) {
            return;
        }
        LOGGER.info("process event: {}", JSON.toJSONString(event));

        //when there has a persisted block on the height, stop sycn this height.If another one is real best block on the height, its next block maybe orphan block, then fetch the real best block as orphan block.
        requestRecord.invalidate(event.getHeight());
        long targetHeight = event.getHeight() + SYNC_BLOCK_TEMP_SIZE;
        if (targetHeight <= getPeersMaxHeight()) {
            sendGetBlock(targetHeight);
        }
        if (isSyncBlockState() && blockService.getMaxHeight() >= getPeersMaxHeight()) {
            systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
            LOGGER.info("sync block finished !");
        }
    }


    @Subscribe
    public void process(SystemStatusEvent event) {
        LOGGER.info("process SystemStatusEvent: {}", JSON.toJSONString(event));
        SystemStatus state = event.getSystemStatus();
        if (SystemStatus.RUNNING == state) {
            syncState = 2;
        }
    }

    @Subscribe
    public void process(ReceiveOrphanBlockEvent event) {
        LOGGER.info("process ReceiveOrphanBlockEvent: {}", JSON.toJSONString(event));
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

        //get block by hash
        if (null != hash) {
            requestRecord.get(height, v -> {
                messageCenter.unicast(sourceId, new GetBlock(height, hash));
                LOGGER.info("send block request! height:{} ", height);
                return sourceId;
            });
            return;
        }

        //get all blocks by height
        sendInitRequest();

    }

    private void dealTimeOut(long height, String sourceId) {
        if (height <= blockService.getMaxHeight()) {
            return;
        }
        LOGGER.info("time out, remove it .sourceId:{} ", sourceId);
        removePeer(sourceId);
        if (!sendGetBlock(height)) {
            if (blockService.getMaxHeight() >= getPeersMaxHeight() && isSyncBlockState()) {
                systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
                LOGGER.info("sync block finished !");
            }
        }
    }

    private void removePeer(String sourceId) {
        connectionManager.remove(sourceId);
        peersMaxHeight.remove(sourceId);
    }

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
        long myHeight = blockService.getMaxHeight();
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


