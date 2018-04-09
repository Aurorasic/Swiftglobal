package cn.primeledger.cas.global.network.socket.message;

import cn.primeledger.cas.global.network.Peer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hello message as the request message for {@link HelloAckMessage}. It will be sent
 * by the client when the connection with the peer is active.
 *
 * @author yuanjiantao
 * @date Created in 2/27/2018
 */
@Data
@NoArgsConstructor
public class HelloMessage extends BaseMessage {

    private Peer peer;

    public HelloMessage(Peer peer) {
        this.peer = peer;
    }
}
