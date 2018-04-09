package cn.primeledger.cas.global.network.socket;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Message cache can avoid the large same messages to be processed at one time.
 * </p>
 *
 * @author baizhengwen
 * @date 2018/3/23
 */
@Component
public class MessageCache {

    private Cache<String, String> cache;

    public MessageCache() {
        cache = Caffeine.newBuilder().maximumSize(Integer.MAX_VALUE)
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .build();
    }

    public boolean isCached(String peerId, String messageHash) {
        String key = peerId + ":" + messageHash;
        String value = cache.getIfPresent(key);
        if (null == value) {
            cache.put(key, key);
            return false;
        }
        return true;
    }

}
