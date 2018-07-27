package com.higgsblock.global.chain.app.net.event;

import com.higgsblock.global.chain.app.net.connection.Connection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author baizhengwen
 * @date 2018-07-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionActivatedEvent {
    private Connection connection;
}
