package com.higgsblock.global.chain.network.socket;

import com.higgsblock.global.chain.network.socket.connection.Connection;

/**
 * Common handler methods for either connection side.
 *
 * @author chenjiawei
 * @date 2018-05-26
 */
public abstract class BaseConnectionHandler {
    /**
     * Triggered when channel attached to connection is closed.
     *
     * @param connection connection to close
     */
    public void onChannelClosed(Connection connection) {
    }
}
