package cn.primeledger.cas.global.config;

import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import cn.primeledger.cas.global.common.handler.IEntityHandler;
import cn.primeledger.cas.global.constants.EntityType;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018/3/8
 */
@Configuration
public class EntityConfig {

    @Bean
    public Map<EntityType, IEntityFormatter> entityFormatterMap(List<IEntityFormatter> list) {
        Map<EntityType, IEntityFormatter> map = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(formatter -> map.put(formatter.getType(), formatter));
        }
        return map;
    }

    @Bean
    public Map<EntityType, IEntityHandler> entityHandlerMap(List<IEntityHandler> list) {
        Map<EntityType, IEntityHandler> map = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(handler -> map.put(handler.getType(), handler));
        }
        return map;
    }
}
