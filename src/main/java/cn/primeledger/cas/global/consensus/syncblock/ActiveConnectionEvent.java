package cn.primeledger.cas.global.consensus.syncblock;

import cn.primeledger.cas.global.network.socket.connection.Connection;
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
