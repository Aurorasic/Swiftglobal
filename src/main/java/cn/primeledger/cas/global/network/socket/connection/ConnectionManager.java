package cn.primeledger.cas.global.network.socket.connection;

import cn.primeledger.cas.global.network.Peer;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The connection manager holds all channels. which manages the all channels' state and count. It has
 * two connection list. One is all connection list, which contains the connection before handshaking with
 * ping message.The other one is active connection, which contains the connection after handshaking with
 * ping message.
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class ConnectionManager {

    // todo baizhengwen add to properties file
    private static final int CLIENT_CONN_LIMIT = 5;
    private static final int SERVER_CONN_LIMIT = 6;

    private Map<String, Connection> connectionMap;

    public ConnectionManager() {
        this.connectionMap = Maps.newConcurrentMap();
    }

    public Connection newConnection(String id, boolean isClient) {
        if (canConnect(isClient)) {
            return connectionMap.computeIfAbsent(id, s -> new Connection(id, isClient));
        }
        return null;
    }

    public synchronized void active(Peer peer, ChannelHandlerContext context) {
        String channelId = context.channel().id().toString();
        Connection connection = getConnectionById(channelId);
        if (null == connection) {
            return;
        }

        String peerId = peer.getId();
        Connection activatedConnection = getConnectionByPeerId(peerId);
        if (null == activatedConnection || Objects.equals(connection, activatedConnection)) {
            connection.active(peer, context);
            return;
        }
        close(connection);
    }

    public void close(String peerId) {
        close(getConnectionByPeerId(peerId));
    }

    /**
     * close and remove connection from the connection map.
     *
     * @param connection
     */
    public void close(Connection connection) {
        String id = connection.getId();
        if (null != connection) {
            connection.close();
        }
        connectionMap.remove(id);
        LOGGER.info("closed connection, id = {}", id);
    }

    public boolean isConnected(String peerId) {
        return getActivatedConnections().stream().anyMatch(channel -> StringUtils.equals(channel.getPeerId(), peerId));
    }

    public boolean canConnect(boolean isClient) {
        if (isClient && countClientConnections() >= CLIENT_CONN_LIMIT) {
            LOGGER.info("client connections is full");
            return false;
        }
        if (!isClient && countServerConnections() >= SERVER_CONN_LIMIT) {
            LOGGER.info("server connections is full");
            return false;
        }
        return true;
    }

    /**
     * Get the active peers.
     */
    public List<Peer> getActivatedPeers() {
        return getActivatedConnections().stream().map(Connection::getPeer).collect(Collectors.toList());
    }

    /**
     * Get the active channels.
     */
    public List<Connection> getActivatedConnections() {
        return connectionMap.values().stream().filter(channel -> channel.isActivated()).collect(Collectors.toList());
    }

    public List<Connection> getInactiveConnections() {
        return connectionMap.values().stream().filter(channel -> !channel.isActivated()).collect(Collectors.toList());
    }

    public int cleanInactiveConnections() {
        List<Connection> list = getInactiveConnections();
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(connection -> close(connection));
        }
        return list.size();
    }

    public Connection getConnectionById(String id) {
        return connectionMap.get(id);
    }

    public Connection getConnectionByPeerId(String peerId) {
        return getActivatedConnections().stream().filter(channel -> StringUtils.equals(peerId, channel.getPeerId())).findFirst().orElse(null);
    }

    public int countClientConnections() {
        return (int) connectionMap.values().stream().filter(Connection::isClient).count();
    }

    public int countServerConnections() {
        return (int) connectionMap.values().stream().filter(connection -> !connection.isClient()).count();
    }
}
