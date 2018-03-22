package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.blockchain.handler.BlockHandler;
import cn.primeledger.cas.global.common.entity.StringMessageEntity;
import cn.primeledger.cas.global.common.entity.UnicastMessageEntity;
import cn.primeledger.cas.global.common.formatter.IEntityFormatter;
import cn.primeledger.cas.global.common.handler.IEntityHandler;
import cn.primeledger.cas.global.constants.EntityType;
import cn.primeledger.cas.global.utils.ExecutorServices;
import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Slf4j
@Component
public class EntityProcessor implements InitializingBean {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Map<EntityType, IEntityFormatter> entityFormatterMap;

    @Autowired
    private Map<EntityType, IEntityHandler> entityHandlerMap;

    private boolean needWaitNextEmpty = false;

    private BlockingQueue<StringMessageEntity> queue = Queues.newLinkedBlockingQueue(1000);

    private ExecutorService messageParseThreadPool = ExecutorServices.newSingleThreadExecutor("MessageParseThreadPool", 10000);
    private ExecutorService messageProcessThreadPool = ExecutorServices.newFixedThreadPool("MessageProcessThreadPool", 4, 10000);
    private ExecutorService blockProcessThreadPool = ExecutorServices.newSingleThreadExecutor("BlockProcessThreadPool", 10000);

    @Override
    public void afterPropertiesSet() throws Exception {
        messageParseThreadPool.submit((Runnable) () -> {
            while (true) {
                process();
            }
        });
    }

    public void add(StringMessageEntity message) {
        if (null != message) {
            queue.offer(message);
        }
    }

    private void process() {
        try {
            UnicastMessageEntity entity = (UnicastMessageEntity) queue.poll(10, TimeUnit.MILLISECONDS);
            //todo yuguojia split this single queue to block queue and transaction queue...
            if (queue.isEmpty() && !needWaitNextEmpty) {
                Collection<IEntityHandler> entityHandlers = entityHandlerMap.values();
                for (IEntityHandler handler : entityHandlers) {
                    if (handler != null && handler instanceof BlockHandler) {
//                        blockProcessThreadPool.submit(() -> {
//                            LOGGER.info("process message is empty");
//                            try {
//                            } catch (Throwable e) {
//                                LOGGER.error(e.getMessage(), e);
//                            }
//                        });

                        handler.queueElementConsumeOver();
                    }
                }
                needWaitNextEmpty = true;
                return;
            }

            if (entity == null) {
                return;
            }

            EntityType entityType = EntityType.getByCode(entity.getType());
            if (null == entityType) {
                return;
            }

            IEntityFormatter formatter = entityFormatterMap.get(entityType);
            Preconditions.checkNotNull(formatter);

            String data = entity.getData();
            Preconditions.checkNotNull(data);

            IEntityHandler handler = entityHandlerMap.get(entityType);
            Preconditions.checkNotNull(handler);

//            messageProcessThreadPool.submit(() -> {
//                LOGGER.info("process handler");
//                try {
//                    if (handler instanceof BlockHandler) {
//                        needWaitNextEmpty = false;
//                    }
//                    //todo yuguojia process block serially
//
//                } catch (Throwable e) {
//                    LOGGER.error(e.getMessage(), e);
//                }
//            });
            handler.process(formatter.parse(data, entity.getVersion()), entity.getVersion(), entity.getSourceId());
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
