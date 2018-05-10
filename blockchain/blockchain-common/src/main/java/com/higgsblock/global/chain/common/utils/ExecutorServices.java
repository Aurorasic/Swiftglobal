package com.higgsblock.global.chain.common.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.*;

/**
 * @author baizhengwen
 * @create 2017-09-26 19:59
 */
public class ExecutorServices {

    private ExecutorServices() {
    }

    public static ExecutorService newSingleThreadExecutor(String name, int taskCapacity) {
        return newFixedThreadPool(name, 1, taskCapacity);
    }

    public static ExecutorService newFixedThreadPool(String name, int nThreads, int taskCapacity) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(taskCapacity),
                createThreadFactory(name));
    }

    public static ScheduledExecutorService newScheduledThreadPool(String name, int nThreads) {
        return new ScheduledThreadPoolExecutor(nThreads, ThreadFactoryUtils.createThreadFactory(name));
    }

    public static ThreadFactory createThreadFactory(String name) {
        if (StringUtils.isNotBlank(name)) {
            return new ThreadFactoryBuilder()
                    .setNameFormat(name + "-pool-%d")
                    .build();
        }
        return Executors.defaultThreadFactory();
    }

}
