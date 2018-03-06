package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.common.entity.StringMessageEntity;
import cn.primeledger.cas.global.common.handler.IEntityHandler;
import cn.primeledger.cas.global.constants.EntityType;
import cn.primeledger.cas.global.utils.ExecutorServices;
import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

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

    private BlockingQueue<StringMessageEntity> queue = Queues.newLinkedBlockingQueue(1000);

    private ExecutorService messageParseThreadPool = ExecutorServices.newSingleThreadExecutor("MessageParseThreadPool", 10000);
    private ExecutorService messageProcessThreadPool = ExecutorServices.newFixedThreadPool("MessageProcessThreadPool", 4, 10000);

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
            StringMessageEntity entity = queue.poll(10, TimeUnit.MILLISECONDS);
            if (entity == null) {
                return;
            }

            IEntityHandler handler = getEntityHandler(entity.getType(), entity.getVersion());
            Preconditions.checkNotNull(handler);

            String data = entity.getData();
            Preconditions.checkNotNull(data);

            messageProcessThreadPool.submit(() -> {
                LOGGER.info("process handler");
                try {
                    handler.process(data);
                } catch (Throwable e) {
                    LOGGER.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private IEntityHandler getEntityHandler(short type, short version) {
        EntityType config = EntityType.getTypeAndVersion(type, version);
        if (null == config) {
            return null;
        }
        String handlerName = config.getHandlerName();
        if (StringUtils.isBlank(handlerName)) {
            return null;
        }
        return context.getBean(handlerName, IEntityHandler.class);
    }
}
