package cn.primeledger.cas.global.schedule;

import cn.primeledger.cas.global.network.Peer;
import cn.primeledger.cas.global.network.PeerManager;
import cn.primeledger.cas.global.network.socket.Client;
import cn.primeledger.cas.global.network.socket.connection.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author baizhengwen
 * @date 2018/3/23
 */
@Slf4j
@Component
public class ConnectionManageTask extends BaseTask {

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private PeerManager peerManager;

    @Autowired
    private Client client;

    @Override
    protected void task() {
        // remove inactive connection
        int inactiveConnections = connectionManager.cleanInactiveConnections();
        LOGGER.info("close {} inactive connections", inactiveConnections);

        if (!connectionManager.canConnect(true)) {
            return;
        }

        List<Peer> peers = peerManager.shuffle(10);
        if (CollectionUtils.isEmpty(peers)) {
            LOGGER.warn("no peers to connect");
            return;
        }

        int newConnNum = 0;
        for (; newConnNum < peers.size(); newConnNum++) {
            client.connect(peers.get(newConnNum));
        }
        LOGGER.info("create {} new connections", newConnNum);
    }

    @Override
    protected long getPeriodMs() {
        return TimeUnit.SECONDS.toMillis(5);
    }
}
