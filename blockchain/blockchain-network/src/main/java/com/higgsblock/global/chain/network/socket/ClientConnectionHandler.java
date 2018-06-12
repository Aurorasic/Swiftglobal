package com.higgsblock.global.chain.network.socket;

import com.higgsblock.global.chain.network.Peer;
import com.higgsblock.global.chain.network.socket.connection.Connection;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Default handler in the procedure this node connect to remote node.
 *
 * @author chenjiawei
 * @date 2018-05-26
 */
public class ClientConnectionHandler extends BaseConnectionHandler {
    /**
     * Triggered when this node connect to remote node successfully.
     *
     * @param peer remote node
     */
    public void onSuccess(Peer peer) {
    }

    /**
     * Triggered when this node connect to remote node and exception happens.
     *
     * @param peer remote node
     */
    public void onCause(Peer peer) {
    }

    /**
     * Triggered when this node connect to remote node and connection is canceled.
     *
     * @param peer remote node
     */
    public void onCancel(Peer peer) {
    }

    /**
     * Triggered when this node connect to remote node and channel is registered.
     *
     * @param channel channel attached to connection
     * @param peer    remote node
     */
    public Connection onChannelInitial(NioSocketChannel channel, Peer peer) {
        return null;
    }
}
