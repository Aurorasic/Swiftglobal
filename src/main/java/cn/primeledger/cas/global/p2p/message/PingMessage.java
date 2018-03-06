package cn.primeledger.cas.global.p2p.message;

import com.google.common.primitives.Longs;

/**
 * @author yuanjiantao
 * @date 2/26/2018
 */
public class PingMessage extends BaseMessage {

    private long timestamp;

    public PingMessage() {
        this.timestamp = System.currentTimeMillis();
        this.encoded = Longs.toByteArray(timestamp);
        this.cmd = MessageType.PING.getCode();
    }

    @Override
    public byte[] getEncoded() {
        return encoded;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return PongMessage.class;
    }

}
