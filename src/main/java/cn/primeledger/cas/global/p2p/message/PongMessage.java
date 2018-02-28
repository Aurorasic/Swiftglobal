package cn.primeledger.cas.global.p2p.message;

import org.spongycastle.util.encoders.Hex;

/**
 * @author yuanjiantao
 * @since 2/23/2018
 **/
public class PongMessage extends BaseMessage {

    private final static byte[] PAYLOAD = Hex.decode("C0");
    // TODO: 2/25/2018 改变payload 为时间戳，并压缩

    @Override
    public byte[] getEncoded() {
        return PAYLOAD;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public String toString() {
        return null;
    }

    public PongMessage(byte[] encoded){
        super(encoded);
    }


}
