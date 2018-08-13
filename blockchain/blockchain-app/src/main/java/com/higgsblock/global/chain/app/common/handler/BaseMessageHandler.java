package com.higgsblock.global.chain.app.common.handler;

import com.google.common.collect.Queues;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import com.higgsblock.global.chain.network.socket.message.IMessage;
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
    private BlockingQueue<IMessage<T>> queue;

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
            this.queue = Queues.newLinkedBlockingQueue(10000);
            this.executorService = executorService;
            this.executorService.execute(() -> {
                while (isRunning) {
                    try {
                        IMessage message = takeMessage();
                        if (null == message) {
                            continue;
                        }
                        LOGGER.debug("take message for processing: {}", message.getData());

                        long validStartTime = System.currentTimeMillis();
                        boolean isValid = valid(message);
                        long validEndTime = System.currentTimeMillis();
                        LOGGER.info("valid spend time :{}ms", validEndTime - validStartTime);

                        if (isValid) {
                            process(message);
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
        if (isRunning) {
            executorService.shutdown();
            executorService = null;
            queue = null;
            isRunning = false;
        }
    }

    @Override
    public synchronized final boolean accept(IMessage<T> message) {
        if (isRunning && null != queue) {
            LOGGER.info("handler class:{}, queue size:{}", getClass().getSimpleName(), queue.size());
            return queue.offer(message);
        }
        return false;
    }

    protected abstract boolean valid(IMessage<T> message);

    protected abstract void process(IMessage<T> message);

    private IMessage takeMessage() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
