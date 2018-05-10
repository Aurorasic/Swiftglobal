package com.higgsblock.global.chain.network.socket.event;

import com.higgsblock.global.chain.network.socket.connection.Connection;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yuanjiantao
 * @date Created on 3/26/2018
 */
@Data
@AllArgsConstructor
public class ActiveConnectionEvent {
    private Connection connection;
}
