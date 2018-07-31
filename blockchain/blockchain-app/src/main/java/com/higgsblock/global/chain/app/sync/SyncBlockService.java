package com.higgsblock.global.chain.app.sync;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.SystemStatus;
import com.higgsblock.global.chain.app.common.SystemStatusManager;
import com.higgsblock.global.chain.app.common.SystemStepEnum;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.common.event.SyncBlockEvent;
import com.higgsblock.global.chain.app.common.event.SystemStatusEvent;
import com.higgsblock.global.chain.app.net.connection.ConnectionManager;
import com.higgsblock.global.chain.app.sync.message.BlockRequest;
import com.higgsblock.global.chain.app.sync.message.MaxHeightRequest;
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Component
@Slf4j
public class SyncBlockService implements IEventBusListener, InitializingBean {

    private static final int SYNC_BLOCK_TEMP_SIZE = 10;

    private static final int MIN_PEER_NUM = 1;

    private static final int SYNC_BLOCK_EXPIRATION = 15;

    private static final long SYNC_BLOCK_IGNORE_NUMBER = 100L;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private IBlockChainService blockChain;

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private SystemStatusManager systemStatusManager;

    private ConcurrentHashMap<String, Long> peersMaxHeight = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledExecutorService = ExecutorServices.newScheduledThreadPool("cache clean up", 1);
    /**
     * 1 represents syncing block in init state
     * 2 represents syncing block in running state
     */
    private int syncState = 1;
    private Cache<BlockRequest, String> requestRecord = Caffeine.newBuilder().maximumSize(100)
            .expireAfterWrite(SYNC_BLOCK_EXPIRATION, TimeUnit.SECONDS)
            .removalListener((RemovalListener<BlockRequest, String>) (request, sourceId, cause) -> {
                if (cause.wasEvicted() && null != request) {
                    dealTimeOut(request, sourceId);
                }
            })
            .build();

    private boolean isSyncBlockState() {
        return syncState == 1;
    }

    public void startSyncBlock() {
        /*
        1.At the beginning of synchronization, ask the nodes you have already connected to about their max height
         */
        messageCenter.broadcast(new MaxHeightRequest());
    }

    private void sendInitRequest() {
        long initHeight = blockChain.getMaxHeight();

        for (long i = 1; i <= SYNC_BLOCK_TEMP_SIZE && i + initHeight <= getPeersMaxHeight(); i++) {
            sendGetBlock(initHeight + i, null);
        }
    }

    private boolean sendGetBlock(long height, String hash) {
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
        requestRecord.get(new BlockRequest(height, hash), v -> {
            messageCenter.unicast(sourceId, new BlockRequest(height, hash));
            LOGGER.info("send block request! height={},hash={} ", height, hash);
            return sourceId;
        });
        return true;
    }

    @Subscribe
    public void process(BlockPersistedEvent event) {
        LOGGER.info("process event: {}", event);
        continueSyncBlock(event);
    }

    /**
     * check whether to continue sync block
     */
    private void continueSyncBlock(BlockPersistedEvent event) {
        tryToChangeSysStatusToRunning();
        requestRecord.invalidate(new BlockRequest(event.getHeight(), event.getBlockHash()));

        //when there has a persisted block on the height, stop sync this height.If another one is real best block on
        // the height, its next block maybe orphan block, then fetch the real best block as orphan block.
        long peerMaxHeight = getPeersMaxHeight();
        long targetHeight = event.getHeight() + SYNC_BLOCK_TEMP_SIZE;
        if (targetHeight <= peerMaxHeight) {
            sendGetBlock(targetHeight, null);
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
    public void process(SyncBlockEvent event) {
        LOGGER.info("process SyncBlockEvent: {}", event);
        long height = event.getHeight();
        String sourceId = event.getSourceId();
        String hash = event.getBlockHash();

        //update peer's max height
        if (null != sourceId) {
            peersMaxHeight.compute(sourceId, (k, v) -> {
                if (null == v) {
                    return height;
                } else {
                    return height > v ? height : v;
                }
            });
        }


        if (null != requestRecord.getIfPresent(new BlockRequest(height, hash))) {
            return;
        }

        if (null != hash && blockChain.isExistBlock(hash)) {
            return;
        }

        if (height <= blockChain.getMaxHeight()) {
            sendGetBlock(height, hash);
            return;
        }

        //get all blocks by height
        sendInitRequest();
    }

    private void dealTimeOut(BlockRequest request, String sourceId) {

        String blockHash = request.getHash();
        long height = request.getHeight();
        if (null != blockHash && blockChain.isExistBlock(blockHash)) {
            return;
        }

        long myHeight = blockChain.getMaxHeight();
        if (height <= myHeight) {
            return;
        }
        LOGGER.info("time out, remove it .sourceId:{} ", sourceId);
        removePeer(sourceId);
        if (sendGetBlock(height, blockHash)) {
            return;
        }
        tryToChangeSysStatusToRunning();
    }

    private void removePeer(String sourceId) {
        connectionManager.removeByPeerId(sourceId);
        peersMaxHeight.remove(sourceId);
    }

    /**
     * The node takes the initiative to ask the adjacent node for the current block height of the other party
     *
     * @param height
     * @param sourceId
     */
    public void updatePeersMaxHeight(long height, String sourceId) {

        if (null == sourceId || height < 1L) {
            return;
        }
        peersMaxHeight.compute(sourceId, (k, v) -> {
            LOGGER.info(" update peer's max height: {} to {}, sourceId is {}", v, height, sourceId);
            if (null == v) {
                return height;
            } else {
                return height > v ? height : v;
            }
        });

        if (!tryToChangeSysStatusToRunning()) {
            sendInitRequest();
        }
    }

    private boolean tryToChangeSysStatusToRunning() {
        long localMaxHeight = blockChain.getMaxHeight();
        long peerMaxHeight = getPeersMaxHeight();
        int peersMaxHeightSize = peersMaxHeight.size();
        if (isSyncBlockState() && peersMaxHeightSize >= MIN_PEER_NUM
                && localMaxHeight > peerMaxHeight - SYNC_BLOCK_IGNORE_NUMBER) {
            systemStatusManager.setSysStep(SystemStepEnum.SYNCED_BLOCKS);
            LOGGER.warn("syncState={},peersMaxHeight.size={},localMaxHeight={},peerMaxHeight={}", syncState, peersMaxHeightSize, localMaxHeight, peerMaxHeight);
            LOGGER.info("sync block finished !");
            return true;
        }
        return false;
    }

    private long getPeersMaxHeight() {
        return peersMaxHeight.values().stream().reduce(1L, (a, b) -> a > b ? a : b);
    }

    @Override
    public void afterPropertiesSet() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> requestRecord.cleanUp(), 20, SYNC_BLOCK_EXPIRATION / 5, TimeUnit.SECONDS);
    }
}


