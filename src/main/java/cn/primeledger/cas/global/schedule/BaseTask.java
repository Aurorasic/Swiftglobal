package cn.primeledger.cas.global.schedule;

import cn.primeledger.cas.global.utils.ThreadFactoryUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @date 2018/3/23
 */
@Slf4j
public abstract class BaseTask {

    private ScheduledExecutorService executorService;

    public synchronized void start() {
        if (null == executorService) {
            executorService = Executors.newScheduledThreadPool(1, ThreadFactoryUtils.createThreadFactory(getClass().getName()));
            executorService.scheduleWithFixedDelay(this::doTask, 0, getPeriodMs(), TimeUnit.MILLISECONDS);
            LOGGER.info("{} started", getClass().getName());
        }
    }

    public synchronized void stop() {
        if (null != executorService) {
            executorService.shutdown();
            executorService = null;
        }
    }

    private void doTask() {
        LOGGER.info("task of {} start", getClass().getName());
        try {
            task();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("task of {} end", getClass().getName());
    }

    protected abstract void task();

    protected abstract long getPeriodMs();
}
