package com.higgsblock.global.chain.app.keyvalue.repository.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.config.ParsingUtils;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.map.MapKeyValueAdapter;
import org.springframework.data.map.repository.config.MapRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

import java.util.Map;

/**
 * @author baizhengwen
 * @date 2018-08-30
 */
public class LevelDbRepositoryConfigurationExtension extends MapRepositoryConfigurationExtension {
    @Override
    protected AbstractBeanDefinition getDefaultKeyValueTemplateBeanDefinition(RepositoryConfigurationSource configurationSource) {
        BeanDefinitionBuilder adapterBuilder = BeanDefinitionBuilder.rootBeanDefinition(MapKeyValueAdapter.class);
        Class<? extends Map> mapType = (Class<? extends Map>) ((AnnotationMetadata) configurationSource.getSource()).getAnnotationAttributes(
                EnableLevelDbRepositories.class.getName()).get("mapType");
        adapterBuilder.addConstructorArgValue(mapType);

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(KeyValueTemplate.class);
        builder.addConstructorArgValue(ParsingUtils.getSourceBeanDefinition(adapterBuilder, configurationSource.getSource()));
        builder.setRole(BeanDefinition.ROLE_SUPPORT);

        return ParsingUtils.getSourceBeanDefinition(builder, configurationSource.getSource());
    }
}
