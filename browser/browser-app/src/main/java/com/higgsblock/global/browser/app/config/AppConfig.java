package com.higgsblock.global.browser.app.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * system config
 *
 * @author baizhengwen
 * @create 2017-03-07 19:32
 */

@Getter
@Configuration
@PropertySource(value = "${spring.config.location}", name = "appConf")
public class AppConfig {

    @Value("${remote.ip}")
    private String remoteIp;

    @Value("${remote.port}")
    private Integer remotePort;

    @Autowired
    private Environment environment;

    public String getValue(String key) {
        return environment.getProperty(key);
    }

    public <T> T getValue(String key, Class<T> clazz) {
        return environment.getProperty(key, clazz);
    }
}
