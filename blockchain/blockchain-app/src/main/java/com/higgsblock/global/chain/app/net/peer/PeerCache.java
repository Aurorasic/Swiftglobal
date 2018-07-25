package com.higgsblock.global.chain.app.net.peer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.higgsblock.global.chain.app.net.peer.Peer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * The temporary cache of the deleted node to prevent peers from being acquired from the adjacent peer
 * node may be exchanged for the peer node that has just been deleted.
 *
 * @author kongyu
 * @date 2018-4-26 17:11
 */
@Component
public class PeerCache {
    private static Cache<String, String> cache;

    public PeerCache() {
        cache = Caffeine.newBuilder().maximumSize(Integer.MAX_VALUE)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    public synchronized boolean isCached(Peer peer) {
        if (null == peer) {
            return false;
        }
        String key = peer.getId();
        String value = cache.getIfPresent(key);
        if (null == value) {
            return false;
        }
        return true;
    }

    public synchronized boolean setCached(Peer peer) {
        if (null == peer) {
            return false;
        }
        String key = peer.getId();
        String value = cache.getIfPresent(key);
        if (null == value) {
            cache.put(key, key);
            return true;
        }
        return false;
    }
}
