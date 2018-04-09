package cn.primeledger.cas.global.config;

import cn.primeledger.cas.global.common.listener.IEventBusListener;
import cn.primeledger.cas.global.utils.ExecutorServices;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author baizhengwen
 * @date 2018/3/27
 */
@Configuration
public class EventBusConfig {

    @Bean
    public EventBus asyncEventBus(List<IEventBusListener> eventBusListeners) {
        AsyncEventBus eventBus = new AsyncEventBus(ExecutorServices.newFixedThreadPool(
                "AsyncEventBus", Runtime.getRuntime().availableProcessors() * 2, 10000
        ));
        CollectionUtils.forAllDo(eventBusListeners, eventBus::register);
        return eventBus;
    }
}
