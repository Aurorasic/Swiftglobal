package cn.primeledger.cas.global.p2p.message;

import lombok.NoArgsConstructor;
import org.spongycastle.util.encoders.Hex;

/**
 * 
 *
 * @author yuanjiantao
 * @date  2/26/2018
 */
@NoArgsConstructor
public class PingMessage extends BaseMessage {

    private final static byte[] PAYLOAD = Hex.decode("C0");

    @Override
    public byte[] getEncoded() {
        return PAYLOAD;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return PongMessage.class;
    }

    @Override
    public String toString() {
        return null;
    }

    public PingMessage(byte[] encoded) {
        super(encoded);
    }

}
