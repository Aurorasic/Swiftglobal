package com.higgsblock.global.chain.app.blockchain.listener;

import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.app.common.message.MessageFormatter;
import com.higgsblock.global.chain.app.common.message.MessageHandler;
import com.higgsblock.global.chain.app.net.ConnectionManager;
import com.higgsblock.global.chain.network.socket.MessageCache;
import com.higgsblock.global.chain.network.socket.connection.Connection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Component
@Slf4j
public class MessageCenter {
    private static final int MIN_WITNESS_NUM = 2;

    @Autowired
    private MessageFormatter formatter;
    @Autowired
    private MessageHandler handler;
    @Autowired
    private ConnectionManager connectionManager;
    @Autowired
    private MessageCache messageCache;

    public boolean dispatch(Object obj) {
        return handler.accept(new SocketRequest(null, obj));
    }

    public boolean dispatch(String sourceId, String obj) {
        return handler.accept(sourceId, obj);
    }

    public boolean dispatchToWitnesses(Object data) {
        Collection<Connection> witnessConnections = connectionManager.getWitnessConnections();
        LOGGER.info("Witness connections size: {}", witnessConnections.size());

        if (witnessConnections.size() >= MIN_WITNESS_NUM) {
            for (Connection connection : witnessConnections) {
                unicast(connection.getPeerId(), data);
                LOGGER.info("send to witness with address {} and data {}", connection.getPeerId(), data);
            }
            return true;
        } else {
            broadcast(data);
            return false;
        }
    }

    public boolean handshake(String channelId, Object data) {
        try {
            String content = formatter.format(data);
            sendMessage(channelId, content);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean unicast(String sourceId, Object data) {
        try {
            Connection connection = connectionManager.getConnectionByPeerId(sourceId);
            if (connection == null) {
                LOGGER.warn("Connection not found");
                return false;
            }

            String content = formatter.format(data);
            sendMessage(connection, content);

            LOGGER.info("unicast message: {}", content);
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean broadcast(Object data) {
        return broadcast(null, data);
    }

    public boolean broadcast(String[] excludeSourceIds, Object data) {
        try {
            List<Connection> connectionList = connectionManager.getActivatedConnections();
            String content = formatter.format(data);

            //Do not send business message to excluded peers
            for (Connection connection : connectionList) {
                if (!ArrayUtils.contains(excludeSourceIds, connection.getPeerId())) {
                    sendMessage(connection, content);
                }
            }

            LOGGER.info("broadcast message: {}", content);
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Send message with the specific connection.
     */
    private void sendMessage(String channelId, String message) {
        Connection connection = connectionManager.getConnectionByChannelId(channelId);
        if (null != connection) {
            sendMessage(connection, message);
        }
    }

    /**
     * Send message with the specific connection.
     */
    private void sendMessage(Connection connection, String message) {
        if (!messageCache.isCached(connection.getChannelId(), message)) {
            connection.sendMessage(message);
        }
    }

}
