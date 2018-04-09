package cn.primeledger.cas.global.network.socket.listener;

import cn.primeledger.cas.global.common.event.BroadcastEvent;
import cn.primeledger.cas.global.common.event.UnicastEvent;
import cn.primeledger.cas.global.common.listener.IEventBusListener;
import cn.primeledger.cas.global.network.socket.MessageCache;
import cn.primeledger.cas.global.network.socket.connection.Connection;
import cn.primeledger.cas.global.network.socket.connection.ConnectionManager;
import cn.primeledger.cas.global.network.socket.message.BizMessage;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@link SendMessageListener} listens the events from the business layer includes
 * "broadcast" event and "unicast" event.The "broadcast" event will be sent to the
 * all the active peers except the excluding ones. While the "unicast" will be sent
 * to the specific one.
 *
 * @author baizhengwen
 * @date 2018/2/28
 */
@Slf4j
@Component
public class SendMessageListener implements IEventBusListener {

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private MessageCache messageCache;

    /**
     * Subscribe the "broadcast" event.
     */
    @Subscribe
    public void process(BroadcastEvent event) {
        LOGGER.info("Accepted broadcast event");

        List<Connection> connectionList = connectionManager.getActivatedConnections();
        String[] excludeSourceIds = event.getEntity().getExcludeSourceIds();

        //Do not send business message to excluded peers
        for (Connection connection : connectionList) {
            if (!ArrayUtils.contains(excludeSourceIds, connection.getPeerId())) {
                processEvent(connection, event.getEntity().getData());
            }
        }
    }

    /**
     * Subscribe the "unicast" event.
     */
    @Subscribe
    public void process(UnicastEvent event) {
        LOGGER.info("Accepted unicast event");
        String peerAddress = event.getEntity().getSourceId();
        Connection connection = connectionManager.getConnectionByPeerId(peerAddress);

        if (connection == null) {
            LOGGER.warn("Connection not found");
            return;
        }

        processEvent(connection, event.getEntity().getData());
    }

    /**
     * Send message with the specific connections.
     */
    private void processEvent(Connection connection, String data) {
        BizMessage message = new BizMessage();
        message.setData(data);

        if (!messageCache.isCached(connection.getPeerId(), message.getHash())) {
            connection.sendMessage(message);
        }
    }
}
