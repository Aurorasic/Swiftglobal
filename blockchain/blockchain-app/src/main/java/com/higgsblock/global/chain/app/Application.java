package com.higgsblock.global.chain.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgsblock.global.chain.app.context.AppContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author baizhengwen
 * @create 2017-03-07 15:55
 */
@Slf4j
@EnableAutoConfiguration
@ComponentScan({"com.higgsblock.global.chain"})
public class Application {

    public static final int PRE_BLOCK_COUNT = 1;

    @Autowired
    private AppContext appContext;

    public static void main(String[] args) throws Exception {
        // Cyclic reference detection
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        // Include fields with a value of null
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
        // The first level fields are sorted alphabetically
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.SortField.getMask();
        // Nested fields are sorted alphabetically
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.MapSortField.getMask();

        ApplicationContext springContext = SpringApplication.run(Application.class, args);
        Application application = springContext.getBean(Application.class);
        application.start();
    }

    private void start() throws Exception {
        appContext.start();
    }
}
