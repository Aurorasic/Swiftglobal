package com.higgsblock.global.chain.app.config;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.higgsblock.global.chain.app.keyvalue.core.IndexedKeyValueAdapter;
import com.higgsblock.global.chain.app.keyvalue.core.IndexedKeyValueTemplate;
import com.higgsblock.global.chain.app.keyvalue.core.TransactionAwareLevelDbAdapter;
import com.higgsblock.global.chain.app.keyvalue.repository.config.EnableLevelDbRepositories;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.keyvalue.core.KeyValueOperations;
import org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext;

import javax.sql.DataSource;

/**
 * data source config
 *
 * @author baizhengwen
 * @date 2018/2/24
 */
@Configuration
@Slf4j
@EnableLevelDbRepositories(value = "test", keyValueTemplateRef = "keyValueTemplate")
public class DataSourceConfig {

    private static final String DB = "MariaDB";

    @Bean(name = {DB})
    public MariaDB4jSpringService mariaDb(@Value("${mariaDB4j.create.sql}") String createSql,
                                          @Value("${mariaDB4j.init.sql}") String initSql,
                                          @Value("${spring.datasource.username}") String userName,
                                          @Value("${spring.datasource.password}") String password,
                                          @Value("${mariaDB4j.dbName}") String dbName,
                                          @Value("${spring.datasource.url}") String url) {
        MariaDB4jSpringService mariaDb = new MariaDBMyServive(createSql, initSql, userName, password, dbName, url);
        return mariaDb;
    }

    /**
     * If you want to use Flyway/Liqubase, make sure those beans are also depend
     * on the MariaDB service or this bean.
     *
     * @return
     */
    @Bean
    @DependsOn(DB)
    public DataSource dataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    public KeyValueOperations keyValueTemplate(IndexedKeyValueAdapter keyValueAdapter) {
        return new IndexedKeyValueTemplate(keyValueAdapter, new KeyValueMappingContext());
    }

    @Bean
    public IndexedKeyValueAdapter keyValueAdapter(AppConfig config) {
        return new TransactionAwareLevelDbAdapter(config.getDataPath());
    }

}