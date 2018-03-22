package cn.primeledger.cas.global.p2p.message;

import cn.primeledger.cas.global.p2p.Peer;
import lombok.NoArgsConstructor;
import org.springframework.util.SerializationUtils;

import java.util.LinkedList;


@NoArgsConstructor
public class PeersMessage extends BaseMessage<LinkedList<Peer>> {

    public PeersMessage(LinkedList<Peer> peers) {
        super(MessageType.PEERS.getCode(), peers);
    }

    public PeersMessage(byte[] encoded) {
        this((LinkedList<Peer>) SerializationUtils.deserialize(encoded));
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }
}
