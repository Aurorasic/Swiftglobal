package cn.primeledger.cas.global.p2p.discover;

import cn.primeledger.cas.global.config.Network;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Start the unpn task.
 * @author zhao xiaogang
 */
public class UpnpMgr {

    private ThreadPoolExecutor executor;
    private Network network;

    public UpnpMgr(Network network) {
        this.executor = new ThreadPoolExecutor(0, 5,
                10, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), new ThreadFactory() {
            AtomicInteger atomicInteger = new AtomicInteger(0);

            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r, "UPNP-MGR-" + atomicInteger.getAndIncrement());
            }
        });
        this.network = network;
    }

    public void start() {
        executor.submit(new UpnpDiscovery(network));
    }

    public void shutdown() {
        executor.shutdown();
    }
}
