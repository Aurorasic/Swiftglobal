package com.higgsblock.global.chain.app.connection;

import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.PeerManager;
import com.higgsblock.global.chain.network.socket.ClientConnectionHandler;
import com.higgsblock.global.chain.network.socket.connection.Connection;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * An object of this class is used for costumer to inject its policy into procedure of connection
 * building on the client side.
 *
 * @author chenjiawei
 * @date 2018-05-26
 */
@Slf4j
public class ClientHandler extends ClientConnectionHandler {
    private PeerManager peerManager;
    private ConnectionManager connectionManager;

    public ClientHandler(PeerManager peerManager, ConnectionManager connectionManager) {
        this.peerManager = peerManager;
        this.connectionManager = connectionManager;
    }

    @Override
    public void onSuccess(Peer peer) {
        peerManager.clearPeerRetries(peer);
    }

    @Override
    public void onCause(Peer peer) {
        if (peerManager.isWitness(peer)) {
            return;
        }
        peerManager.onTryCompleted(peer);

        // Connection may be created before connection fails.
        connectionManager.remove(peer.getId());
    }

    @Override
    public void onCancel(Peer peer) {
        if (peerManager.isWitness(peer)) {
            return;
        }
        peerManager.onTryCompleted(peer);

        // Connection may be created before connection fails.
        connectionManager.remove(peer.getId());
    }

    @Override
    public Connection onChannelInitial(NioSocketChannel channel, Peer peer) {
        return connectionManager.createConnection(channel, peer, true);
    }

    @Override
    public void onChannelClosed(Connection connection) {
        connectionManager.remove(connection);
    }
}
