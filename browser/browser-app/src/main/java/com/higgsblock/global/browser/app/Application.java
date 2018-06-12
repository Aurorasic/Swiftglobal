package com.higgsblock.global.browser.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.higgsblock.global.chain.app.schedule.BaseTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

/**
 * @author baizhengwen
 * @create 2017-03-07 15:55
 */
@Slf4j
@EnableAutoConfiguration
@ComponentScan({"com.higgsblock.global.browser"})
public class Application {

    @Autowired
    private List<BaseTask> tasks;

    public static void main(String[] args) throws Exception {
        // Cyclic reference detection
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        // Include fields with a value of null
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteMapNullValue.getMask();
        // The first level fields are sorted alphabetically
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.SortField.getMask();
        // Nested fields are sorted alphabetically
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.MapSortField.getMask();

        ApplicationContext context = SpringApplication.run(Application.class, args);

        Application application = context.getBean(Application.class);
        application.start(context);

    }


    private void start(ApplicationContext context) throws Exception {
        //定时拉取区块并保存到本地
        startPullBlockDataTimerTasks();
    }

    private void startPullBlockDataTimerTasks() {
        if (CollectionUtils.isNotEmpty(tasks)) {
            tasks.forEach(BaseTask::start);
        }
    }
}
