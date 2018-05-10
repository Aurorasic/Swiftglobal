package com.higgsblock.global.chain.app.config;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author baizhengwen
 * @date 2018/3/27
 */
@Configuration
public class EventBusConfig {

    @Bean
    public EventBus asyncEventBus() {
        AsyncEventBus eventBus = new AsyncEventBus(ExecutorServices.newFixedThreadPool(
                "AsyncEventBus", Runtime.getRuntime().availableProcessors() * 2, 10000
        ));
        return eventBus;
    }
}
