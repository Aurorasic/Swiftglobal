package com.higgsblock.global.chain.app.config;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

/**
 * data source config
 *
 * @author baizhengwen
 * @date 2018/2/24
 */
@Configuration
@Slf4j
@EnableAutoConfiguration
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

}