package cn.primeledger.cas.global.p2p.message;

import com.google.common.primitives.Longs;

/**
 * @author yuanjiantao
 * @since 2/23/2018
 **/
public class PongMessage extends BaseMessage {

    private long timestamp;

    public PongMessage(byte[] encoded) {
        this.encoded = encoded;
        this.timestamp = Longs.fromByteArray(encoded);
        this.cmd = MessageType.PONG.getCode();
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public byte[] getEncoded() {
        return encoded;
    }


}
