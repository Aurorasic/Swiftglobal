package cn.primeledger.cas.global.network.socket.message;

import cn.primeledger.cas.global.network.Peer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Hello ack message as the response message for {@link HelloMessage}. It will be sent
 * by the server when received the hello message from client.
 *
 * @author yuanjiantao
 * @date Created in 2/28/2018
 */

@Data
@NoArgsConstructor
public class HelloAckMessage extends BaseMessage {
    private Peer peer;

    public HelloAckMessage(Peer peer) {
        this.peer = peer;
    }
}
