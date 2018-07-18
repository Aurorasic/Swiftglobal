package com.higgsblock.global.chain.app.net;

import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.socket.ServerConnectionHandler;
import com.higgsblock.global.chain.network.socket.connection.Connection;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * An object of this class is used for costumer to inject its policy into procedure of connection
 * building on the server side.
 *
 * @author chenjiawei
 * @date 2018-05-26
 */
@Slf4j
public class ServerHandler extends ServerConnectionHandler {
    private ConnectionManager connectionManager;

    public ServerHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Connection onChannelInitial(NioSocketChannel channel) {
        if (!connectionManager.connectionNumberAllowedAsServer()) {
            return null;
        }
        return connectionManager.createConnection(channel, false);
    }

    @Override
    public void onChannelClosed(Connection connection) {
        connectionManager.remove(connection);
    }

    @Override
    public void onPeerReceived(Peer peer, Connection connection) {
        if (connection == null) {
            return;
        }

        connectionManager.receivePeer(connection, peer);
    }
}
