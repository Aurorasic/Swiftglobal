package cn.primeledger.cas.global.p2p.message;

import org.spongycastle.util.encoders.Hex;

public class GetPeersMessage extends BaseMessage {

    /**
     * GetPeers message is always a the same single command payload
     */
    private final static byte[] FIXED_PAYLOAD = Hex.decode("C104");

    public GetPeersMessage() {
        this.cmd = MessageType.GET_PEERS.getCode();
    }

    @Override
    public byte[] getEncoded() {
        return FIXED_PAYLOAD;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return PeersMessage.class;
    }
}
