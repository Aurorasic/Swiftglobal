package com.higgsblock.global.chain.app.task;

import com.higgsblock.global.chain.common.utils.ExecutorServices;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The type Base task.
 *
 * @author baizhengwen
 * @date 2018 /3/23
 */
@Slf4j
public abstract class BaseTask {

    /**
     * The Executor service.
     */
    private ScheduledExecutorService executorService;

    /**
     * Start.
     */
    public synchronized void start() {
        if (null == executorService) {
            executorService = ExecutorServices.newScheduledThreadPool(getClass().getName(), 1);
            executorService.scheduleWithFixedDelay(this::doTask, 0, getPeriodMs(), TimeUnit.MILLISECONDS);
            LOGGER.info("{} started", getClass().getName());
        }
    }

    /**
     * Stop.
     */
    public synchronized void stop() {
        if (null != executorService) {
            executorService.shutdown();
            executorService = null;
        }
    }

    /**
     * Do task.
     */
    private void doTask() {
        LOGGER.info("task of {} start", getClass().getName());
        try {
            task();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("task of {} end", getClass().getName());
    }

    /**
     * Task.
     */
    protected abstract void task();

    /**
     * Gets period ms.
     *
     * @return the period ms
     */
    protected abstract long getPeriodMs();
}
