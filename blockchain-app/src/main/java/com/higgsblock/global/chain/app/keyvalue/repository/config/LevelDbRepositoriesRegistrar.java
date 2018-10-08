package com.higgsblock.global.chain.app.keyvalue.repository.config;

import org.springframework.data.map.repository.config.MapRepositoriesRegistrar;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * @author baizhengwen
 * @date 2018-08-30
 */
public class LevelDbRepositoriesRegistrar extends MapRepositoriesRegistrar {
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableLevelDbRepositories.class;
    }

    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new LevelDbRepositoryConfigurationExtension();
    }
}
