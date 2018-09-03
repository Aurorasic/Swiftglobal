package com.higgsblock.global.chain.common.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author baizhengwen
 * @create 2017-09-26 19:59
 */
public class ThreadFactoryUtils {
    private ThreadFactoryUtils() {
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
