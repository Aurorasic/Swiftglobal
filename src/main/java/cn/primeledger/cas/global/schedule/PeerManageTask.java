package cn.primeledger.cas.global.schedule;

import cn.primeledger.cas.global.network.PeerManager;
import cn.primeledger.cas.global.network.socket.connection.Connection;
import cn.primeledger.cas.global.network.socket.connection.ConnectionManager;
import cn.primeledger.cas.global.network.socket.message.GetPeersMessage;
import lombok.extern.slf4j.Slf4j;
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
public class PeerManageTask extends BaseTask {

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private PeerManager peerManager;

    @Override
    protected void task() {
        // get seed peers from registry center if there are less then 2 peers
        if (peerManager.count() < 2) {
            peerManager.getSeedPeers();
        }

        List<Connection> connections = connectionManager.getActivatedConnections();
        for (Connection connection : connections) {
            connection.sendMessage(new GetPeersMessage());
        }
    }

    @Override
    protected long getPeriodMs() {
        return TimeUnit.SECONDS.toMillis(30);
    }
}
