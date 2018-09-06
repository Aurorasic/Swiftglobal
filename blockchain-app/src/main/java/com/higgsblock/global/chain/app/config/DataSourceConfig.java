package com.higgsblock.global.chain.app.config;

import com.higgsblock.global.chain.app.keyvalue.core.IndexedKeyValueAdapter;
import com.higgsblock.global.chain.app.keyvalue.core.IndexedKeyValueTemplate;
import com.higgsblock.global.chain.app.keyvalue.core.TransactionAwareLevelDbAdapter;
import com.higgsblock.global.chain.app.keyvalue.repository.config.EnableLevelDbRepositories;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext;

/**
 * data source config
 *
 * @author baizhengwen
 * @date 2018/2/24
 */
@Configuration
@Slf4j
@EnableLevelDbRepositories(value = "com.higgsblock.global.chain", keyValueTemplateRef = "keyValueTemplate")
public class DataSourceConfig {

    @Bean
    public KeyValueOperations keyValueTemplate(IndexedKeyValueAdapter keyValueAdapter) {
        return new IndexedKeyValueTemplate(keyValueAdapter, new KeyValueMappingContext());
    }

    @Bean
    public IndexedKeyValueAdapter keyValueAdapter(AppConfig config) {
        return new TransactionAwareLevelDbAdapter(config.getDataPath());
    }

}