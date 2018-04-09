package cn.primeledger.cas.global.network.socket.message;

import cn.primeledger.cas.global.network.Peer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

/**
 * Peer message as the response message for {@link GetPeersMessage}.
 */

@Data
@NoArgsConstructor
public class PeersMessage extends BaseMessage {
    private LinkedList<Peer> peers;

    public PeersMessage(LinkedList<Peer> peers) {
        this.peers = peers;
    }
}
