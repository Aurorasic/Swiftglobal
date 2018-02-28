package cn.primeledger.cas.global.p2p.message;

import cn.primeledger.cas.global.p2p.Peer;

/**
 * @author yuanjiantao
 * @date Created in 2/27/2018
 */
public class HelloMessage extends BaseMessage {

    private Peer peer;
    private long timestamp;

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public String toString() {
        return null;
    }
}
