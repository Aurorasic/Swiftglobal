package com.higgsblock.global.chain.network.socket;

import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.socket.connection.Connection;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Default handler in the procedure this node accept connection from remote node.
 *
 * @author chenjiawei
 * @date 2018-05-26
 */
public class ServerConnectionHandler extends BaseConnectionHandler {
    /**
     * Triggered when this node connect to remote node and channel is registered.
     *
     * @param channel channel attached to connection
     */
    public Connection onChannelInitial(NioSocketChannel channel) {
        return null;
    }

    /**
     * Triggered when this node receives peer information.
     *
     * @param peer       peer information from client
     * @param connection connection via which peer information is received
     */
    public void onPeerReceived(Peer peer, Connection connection) {
    }
}
