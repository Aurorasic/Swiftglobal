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
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import com.higgsblock.global.chain.network.socket.connection.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
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
public class SyncBlockService implements IEventBusListener {

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
        scheduledExecutorService.schedule(() -> requestRecord.cleanUp(), 3, TimeUnit.SECONDS);
        try {
            countDownLatch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if (peersMaxHeight.size() == 0 || getPeersMaxHeight() <= blockService.getBestMaxHeight()) {
            systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
            LOGGER.info("there is no need to sync block, sync block finished! peers size :{} my max height : {} , peers' max height:{}"
                    , peersMaxHeight.size(), blockService.getBestMaxHeight(), getPeersMaxHeight());
            return;
        }

        sendInitRequest();
        LOGGER.info("send init sync block request end!");
    }

    private void sendInitRequest() {
        long initHeight = blockService.getBestMaxHeight();
        for (long i = 1; i <= SYNC_BLOCK_TEMP_SIZE && i + initHeight <= getPeersMaxHeight(); i++) {
            sendGetData(initHeight + i);
        }
    }

    private boolean sendGetData(long height) {
        List<String> list = new ArrayList<>();
        peersMaxHeight.forEach((k, v1) -> {
            if (v1 >= height) {
                list.add(k);
            }
        });
        if (list.size() == 0) {
            return false;
        }
        String sourceId = list.get(new Random().nextInt(list.size()));
        requestRecord.get(height, v -> {
            messageCenter.unicast(sourceId, new GetData(height));
            LOGGER.info("send block request! height:{} ", height);
            return sourceId;
        });
        return true;
    }

    @Subscribe
    public void process(BlockPersistedEvent event) {
        LOGGER.info("process event: {}", JSON.toJSONString(event));
        if (event.hasBestBlock()) {
            requestRecord.invalidate(event.getHeight());
            long targetHeight = event.getHeight() + SYNC_BLOCK_TEMP_SIZE;
            if (targetHeight <= getPeersMaxHeight()) {
                sendGetData(targetHeight);
            }
            if (isSyncBlockState() && blockService.getBestMaxHeight() >= getPeersMaxHeight()) {
                systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
                LOGGER.info("sync block finished !");
            }
        }
    }

    @Subscribe
    public void process(SystemStatusEvent event) {
        LOGGER.info("process event: {}", JSON.toJSONString(event));
        SystemStatus state = event.getSystemStatus();
        if (SystemStatus.RUNNING == state) {
            syncState = 2;
        }
    }

    @Subscribe
    public void process(ReceiveOrphanBlockEvent event) {
        LOGGER.info("process event: {}", JSON.toJSONString(event));
        long height = event.getHeight();
        String sourceId = event.getSourceId();
        peersMaxHeight.compute(sourceId, (k, v) -> {
            if (null == v) {
                return height;
            } else {
                return height > v ? height : v;
            }
        });
        sendInitRequest();
    }

    private void dealTimeOut(long height, String sourceId) {
        if (height <= blockService.getBestMaxHeight()) {
            return;
        }
        removePeer(sourceId);
        if (!sendGetData(height)) {
            if (blockService.getBestMaxHeight() >= getPeersMaxHeight() && isSyncBlockState()) {
                systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
                LOGGER.info("sync block finished !");
            }
        }
    }

    private void removePeer(String sourceId) {
        connectionManager.close(sourceId);
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
        long myHeight = blockService.getBestMaxHeight();
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
}


