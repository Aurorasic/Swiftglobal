package cn.primeledger.cas.global.p2p.discover;

import cn.primeledger.cas.global.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Start the unpn task.
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class UpnpMgr {

    @Autowired
    private AppConfig appConfig;

    private ThreadPoolExecutor executor;

    public void start() {
        AtomicInteger atomicInteger = new AtomicInteger(1);
        this.executor = new ThreadPoolExecutor(0, 5,
                10, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), r -> {
            return new Thread(r, "UPNP-MGR-" + atomicInteger.getAndIncrement());
        });

        executor.submit(new UpnpDiscovery(appConfig));
        LOGGER.info("UpnpMgr started");
    }

    public void shutdown() {
        executor.shutdown();
    }

}
