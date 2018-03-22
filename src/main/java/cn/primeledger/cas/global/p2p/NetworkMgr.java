package cn.primeledger.cas.global.p2p;

import cn.primeledger.cas.global.p2p.discover.UpnpMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Network manager is responsible for global management of the p2p network.
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class NetworkMgr {
    private volatile boolean isRunning;

    @Autowired
    private PeerClient p2PClient;

    @Autowired
    private PeerMgr peerMgr;

    @Autowired
    private UpnpMgr upnpMgr;

    public synchronized void start() {
        LOGGER.info("NetworkMgr started");
        if (!isRunning) {
            p2PClient.start();

            peerMgr.start();
            upnpMgr.start();
            isRunning = true;
        }
    }

    public synchronized void shutdown() {
        if (isRunning) {
            peerMgr.shutdown();
            upnpMgr.shutdown();

            isRunning = false;
            LOGGER.info("NetworkMgr shut down");
        }
    }
}
