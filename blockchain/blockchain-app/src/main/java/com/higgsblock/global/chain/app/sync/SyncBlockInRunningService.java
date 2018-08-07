package com.higgsblock.global.chain.app.sync;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.eventbus.Subscribe;
import com.higgsblock.global.chain.app.blockchain.IBlockChainService;
import com.higgsblock.global.chain.app.blockchain.listener.MessageCenter;
import com.higgsblock.global.chain.app.common.event.BlockPersistedEvent;
import com.higgsblock.global.chain.app.common.event.SyncBlockEvent;
import com.higgsblock.global.chain.app.net.connection.ConnectionManager;
import com.higgsblock.global.chain.app.net.peer.Peer;
import com.higgsblock.global.chain.app.sync.message.BlockRequest;
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
public class SyncBlockInRunningService implements IEventBusListener {

    private static final int SYNC_BLOCK_TEMP_SIZE = 10;

    private static final int SYNC_BLOCK_EXPIRATION = 15;

    @Autowired
    private MessageCenter messageCenter;

    @Autowired
    private IBlockChainService blockChain;

    @Autowired
    private ConnectionManager connectionManager;

    private ConcurrentHashMap<String, Long> peersMaxHeight = new ConcurrentHashMap<>();

    private Cache<BlockRequest, String> requestRecord = Caffeine.newBuilder().maximumSize(100)
            .expireAfterWrite(SYNC_BLOCK_EXPIRATION, TimeUnit.SECONDS)
            .removalListener((RemovalListener<BlockRequest, String>) (request, sourceId, cause) -> {
                if (cause.wasEvicted() && null != request) {
                    dealTimeOut(request, sourceId);
                }
            })
            .build();


    private void sendInitRequest() {
        long initHeight = blockChain.getMaxHeight();
        long maxRequestHeight = Long.min(initHeight + SYNC_BLOCK_TEMP_SIZE, getPeersMaxHeight());
        List<String> list = getAvailableConnection(maxRequestHeight);


        for (long height = initHeight + 1; height < maxRequestHeight; height++) {
            final long finalHeight = height;
            if (list.size() == 0) {
                requestRecord.get(new BlockRequest(height, null), v -> {
                    messageCenter.broadcast(new BlockRequest(finalHeight, null));
                    LOGGER.info("send block request! height={},hash={}, current cache size={} ", finalHeight, null, requestRecord.estimatedSize());
                    return "null";
                });
            } else {
                String sourceId = list.get(new Random().nextInt(list.size()));
                requestRecord.get(new BlockRequest(height, null), v -> {
                    messageCenter.unicast(sourceId, new BlockRequest(finalHeight, null));
                    LOGGER.info("send block request! height={},hash={}, current cache size={} ", finalHeight, null, requestRecord.estimatedSize());
                    return sourceId;
                });
            }
        }
    }

    private boolean sendGetBlock(long height, String hash) {

        List<String> list = getAvailableConnection(height);

        if (list.size() == 0) {
            LOGGER.info("have no peer to sync! height={}, current cache size={} ", height, requestRecord.estimatedSize());
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
    public void process(SyncBlockEvent event) {
        LOGGER.info("process SyncBlockEvent: {}", event);
        long height = event.getHeight();
        String sourceId = event.getSourceId();
        String hash = event.getBlockHash();

        //update peer's max height
        if (null != sourceId) {
            peersMaxHeight.compute(sourceId, (k, v) -> {
                LOGGER.info(" update peer's max height: {} to {}, sourceId is {}", v, height, sourceId);
                if (null == v) {
                    return height;
                } else {
                    return height > v ? height : v;
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
                    return "null";
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
    }

    private void removePeer(String sourceId) {
        connectionManager.removeByPeerId(sourceId);
        peersMaxHeight.remove(sourceId);
    }


    private long getPeersMaxHeight() {
        return peersMaxHeight.values().stream().reduce(1L, (a, b) -> a > b ? a : b);
    }

    private List<String> getAvailableConnection(long height) {
        List<String> list = new ArrayList<>();
        List<String> peerList = connectionManager.getActivatedPeers().stream().map(Peer::getId).collect(Collectors.toList());
        for (Map.Entry<String, Long> entry : peersMaxHeight.entrySet()) {
            if (!peerList.contains(entry.getKey())) {
                peersMaxHeight.remove(entry.getKey());
                continue;
            }
            if (entry.getValue() >= height) {
                list.add(entry.getKey());
            }
        }
        return list;
    }
}


