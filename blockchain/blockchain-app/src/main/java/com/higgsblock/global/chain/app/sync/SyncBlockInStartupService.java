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
import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.app.sync.message.BlockRequest;
import com.higgsblock.global.chain.app.sync.message.MaxHeightRequest;
import com.higgsblock.global.chain.common.eventbus.listener.IEventBusListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yuanjiantao
 * @date Created on 3/8/2018
 */
@Component
@Slf4j
public class SyncBlockInStartupService implements IEventBusListener {

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
        long maxRequestHeight = Long.min(initHeight + SYNC_BLOCK_TEMP_SIZE, getPeersMaxHeight());

        List<String> list = getAvailableConnection(maxRequestHeight);
        if (list.size() == 0) {
            LOGGER.info("have no peer to sync! height={}, current cache size={} ", maxRequestHeight, requestRecord.estimatedSize());
            return;
        }

        for (long height = initHeight + 1; height < maxRequestHeight; height++) {
            final long finalHeight = height;
            String sourceId = list.get(new Random().nextInt(list.size()));
            requestRecord.get(new BlockRequest(height, null), v -> {
                messageCenter.unicast(sourceId, new BlockRequest(finalHeight, null));
                LOGGER.info("send block request! height={},hash={}, current cache size={} ", finalHeight, null, requestRecord.estimatedSize());
                return sourceId;
            });
        }
    }

    private boolean sendGetBlock(long height, String hash) {

        List<String> list = getAvailableConnection(height);

        if (list.size() == 0) {
            LOGGER.info("have no peer to sync! height={},,hash={}, current cache size={} ", height, hash, requestRecord.estimatedSize());
            return false;
        }
        String sourceId = list.get(new Random().nextInt(list.size()));
        requestRecord.get(new BlockRequest(height, hash), v -> {
            messageCenter.unicast(sourceId, new BlockRequest(height, hash));
            LOGGER.info("send block request! height={},hash={}, current cache size={} ", height, hash, requestRecord.estimatedSize());
            return sourceId;
        });
        return true;
    }

    @Subscribe
    public void process(BlockPersistedEvent event) {
        LOGGER.info("process event: {}", event);
        continueSyncBlock(event.getHeight());
    }

    /**
     * check whether to continue sync block
     */
    private void continueSyncBlock(long height) {
        tryToChangeSysStatusToRunning();
        requestRecord.invalidate(new BlockRequest(height, null));
        //when there has a persisted block on the height, stop sync this height.If another one is real best block on
        // the height, its next block maybe orphan block, then fetch the real best block as orphan block.
        long peerMaxHeight = getPeersMaxHeight();
        long targetHeight = height + SYNC_BLOCK_TEMP_SIZE;
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
                LOGGER.info(" update peer's max height: {} to {}, sourceId is {}", v, height, sourceId);
                if (null != v) {
                    return height > v ? height : v;
                } else {
                    return height;
                }
            });
        }

        if (null != requestRecord.getIfPresent(new BlockRequest(height, null))) {
            LOGGER.info("block request have send ! height={},hash={}", height, hash);
            return;
        }

        if (null != requestRecord.getIfPresent(new BlockRequest(height, hash))) {
            LOGGER.info("block request have send ! height={},hash={}", height, hash);
            return;
        }

        if (null != hash && blockChain.isExistBlock(hash)) {
            LOGGER.info("block exist ! height={},hash={}", height, hash);
            return;
        }

        if (height <= blockChain.getMaxHeight()) {
            List<String> list = getAvailableConnection(height);

            if (list.size() == 0) {
                requestRecord.get(new BlockRequest(height, hash), v -> {
                    messageCenter.broadcast(new BlockRequest(height, hash));
                    LOGGER.info("send block request! height={},hash={}, current cache size={} ", height, hash, requestRecord.estimatedSize());
                    return null;
                });
            } else {
                final String sourceIdToSync = list.get(new Random().nextInt(list.size()));
                requestRecord.get(new BlockRequest(height, hash), v -> {
                    messageCenter.unicast(sourceIdToSync, new BlockRequest(height, hash));
                    LOGGER.info("send block request! height={},hash={}, current cache size={} ", height, hash, requestRecord.estimatedSize());
                    return sourceIdToSync;
                });
            }
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
            if (null != v) {
                return height > v ? height : v;
            } else {
                return height;
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
            LOGGER.info("sync block finished !syncState={},peersMaxHeight.size={},localMaxHeight={},peerMaxHeight={}", syncState, peersMaxHeightSize, localMaxHeight, peerMaxHeight);
            return true;
        }
        return false;
    }

    private long getPeersMaxHeight() {
        return peersMaxHeight.values().stream().reduce(1L, (a, b) -> a > b ? a : b);
    }

    private List<String> getAvailableConnection(long height) {
        List<String> list = new ArrayList<>();
        List<String> peerList = connectionManager.getActivatedPeers().stream().map(Peer::getId).collect(Collectors.toList());
        for (Map.Entry<String, Long> entry : peersMaxHeight.entrySet()) {
            String key = entry.getKey();
            long value = entry.getValue();
            if (!peerList.contains(key)) {
                peersMaxHeight.remove(key);
                continue;
            }
            if (value >= height) {
                list.add(key);
            }
        }
        return list;
    }
}


