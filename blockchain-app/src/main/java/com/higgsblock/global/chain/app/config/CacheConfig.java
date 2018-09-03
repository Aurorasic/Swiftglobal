package com.higgsblock.global.chain.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * network config
 *
 * @author baizhengwen
 * @date 2018/2/24
 */
@Configuration
@Slf4j
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(AppConfig config) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheSpecification(config.getCacheSpecification());
        return new TransactionAwareCacheManagerProxy(cacheManager);
    }
}