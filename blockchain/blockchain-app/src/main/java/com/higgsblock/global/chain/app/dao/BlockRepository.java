package com.higgsblock.global.chain.app.dao;

import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.collect.Sets;
import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.app.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.h2.mvstore.MVMapConcurrent;
import org.h2.mvstore.MVStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @date 2018-08-08
 */
@Slf4j
@Repository
public class BlockRepository implements IBlockRepository {

    @Autowired
    private AppConfig config;

    private Cache<String, MVStore> storeCache = Caffeine.newBuilder()
            .maximumSize(10)
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .removalListener((RemovalListener<String, MVStore>) (key, store, cause) -> closeStore(store))
            .build();

    @Override
    @CachePut(value = "Block", key = "#block.hash", condition = "null != #block && null != #block.hash")
    @CacheEvict(value = "Block", key = "#block.height", condition = "null != #block && #block.height > 0")
    public boolean save(Block block) {
        try {
            long height = block.getHeight();
            String filename = getFilename(height);
            execute(filename, map -> {
                String data = map.get(height);
                Set<Block> blocks = StringUtils.isEmpty(data)
                        ? Sets.newHashSet()
                        : Sets.newHashSet(JSON.parseArray(data, Block.class));
                blocks.add(block);
                map.put(height, JSON.toJSONString(blocks));
                return null;
            });
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    @Cacheable(value = "Block", key = "#height", condition = "#height > 0")
    public List<Block> findByHeight(long height) {
        String filename = getFilename(height);
        String result = execute(filename, map -> map.get(height));
        return JSON.parseArray(result, Block.class);
    }

    @Override
    @CacheEvict(value = "Block", key = "#height", condition = "#height > 0")
    public boolean deleteByHeight(long height) {
        String filename = getFilename(height);
        String result = execute(filename, map -> map.remove(height));
        return null != result;
    }

    protected synchronized String execute(String filename, OpFunction<Long, String> function) {
        MVStore store = storeCache.get(filename, key -> new MVStore.Builder()
                .compressHigh()
                .fileName(key)
                .open());
        Map<Long, String> map = store.openMap("default", new MVMapConcurrent.Builder<>());
        return (String) function.apply(map);
    }

    protected synchronized void closeStore(MVStore store) {
        if (null != store && !store.isClosed()) {
            store.close();
        }
    }

    protected String getFilename(long height) {
        return String.format("%s/%s", config.getBlockDir(), height / 10000);
    }

    interface OpFunction<K, V> {
        Object apply(Map<K, V> map);
    }
}
