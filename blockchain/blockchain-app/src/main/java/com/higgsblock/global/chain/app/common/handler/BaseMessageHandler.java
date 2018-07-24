package com.higgsblock.global.chain.app.common.handler;

import com.google.common.collect.Queues;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Slf4j
@Data
public abstract class BaseMessageHandler<T> implements IMessageHandler<T> {

    private volatile boolean isRunning;
    private ExecutorService executorService;
    private BlockingQueue<SocketRequest<T>> queue;

    public BaseMessageHandler() {
        this.queue = Queues.newLinkedBlockingQueue(10000);
    }

    @Override
    public Class<T> getMessageClass() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] entityClass = ((ParameterizedType) type).getActualTypeArguments();
            if (null != entityClass) {
                return (Class<T>) entityClass[0];
            }
        }
        return null;
    }

    @Override
    public final synchronized void start() {
        if (!isRunning) {
            start(ExecutorServices.newSingleThreadExecutor(getClass().getName(), 100));
        }
    }

    @Override
    public final synchronized void start(ExecutorService executorService) {
        if (!isRunning) {
            isRunning = true;
            this.executorService = executorService;
            this.executorService.execute(() -> {
                while (isRunning) {
                    try {
                        SocketRequest request = takeRequest();
                        LOGGER.info("take request for processing; {}", request);

                        boolean isValid = check(request);
                        if (isValid) {
                            process(request);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            });
            LOGGER.info("{} started", getClass().getName());
        }
    }

    @Override
    public final synchronized void stop() {
        if (null != executorService) {
            executorService.shutdown();
            executorService = null;
            isRunning = false;
        }
    }

    @Override
    public final boolean accept(SocketRequest<T> request) {
        if (null != queue) {
            return queue.offer(request);
        }
        return false;
    }

    protected abstract boolean check(SocketRequest<T> request);

    protected abstract void process(SocketRequest<T> request);

    private SocketRequest takeRequest() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
