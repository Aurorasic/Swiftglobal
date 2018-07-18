package com.higgsblock.global.chain.app.task;

import com.higgsblock.global.chain.app.net.ConnectionManager;
import com.higgsblock.global.chain.network.socket.connection.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * A periodic task for connection management.
 *
 * @author chenjiawei
 * @date 2018-05-23
 */
@Component
@Slf4j
public class ConnectionManageTask extends BaseTask {
    @Autowired
    private ConnectionManager connectionManager;

    @Override
    protected void task() {
        displayWitnessConnections();

        // Remove every connection which has not received peer information within timeout.
        connectionManager.removePeerUnknownConnections();

        // Refresh level of connections in pool.
        connectionManager.refreshConnectionLevel();

        // Remove extra connections.
        connectionManager.removeExtraConnections();

        // Create special connections.
        connectionManager.createSpecialConnections();

        // Remove specified size of l3-level connections randomly.
        connectionManager.removeL3RandomConnections(2);

        // Get peers randomly and connect to them.
        connectionManager.createRandomConnections();
    }

    private void displayWitnessConnections() {
        Collection<Connection> witnessConnections = connectionManager.getWitnessConnections();
        LOGGER.info("Witness connections size: {}", witnessConnections.size());
    }

    @Override
    protected long getPeriodMs() {
        return TimeUnit.SECONDS.toMillis(5);
    }
}
