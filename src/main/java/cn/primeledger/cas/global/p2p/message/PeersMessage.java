package cn.primeledger.cas.global.p2p.message;

import cn.primeledger.cas.global.p2p.Peer;
import lombok.NoArgsConstructor;
import org.springframework.util.SerializationUtils;

import java.util.Set;


@NoArgsConstructor
public class PeersMessage extends BaseMessage {

    private Set<Peer> peers;

    public PeersMessage(Set<Peer> peers) {
        this.peers = peers;
        this.cmd = MessageType.PEERS.getCode();
        this.encoded = SerializationUtils.serialize(peers);
    }

    public PeersMessage(byte[] encoded) {
        this.encoded = encoded;
        this.cmd = MessageType.PEERS.getCode();
        this.peers = (Set<Peer>) SerializationUtils.deserialize(encoded);
    }

    public Set<Peer> getPeers() {
        return peers;
    }

    @Override
    public byte[] getEncoded() {
        return encoded;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }
}
