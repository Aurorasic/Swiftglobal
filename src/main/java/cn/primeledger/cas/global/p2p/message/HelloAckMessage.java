package cn.primeledger.cas.global.p2p.message;

import cn.primeledger.cas.global.utils.ProtoBufUtil;
import com.google.common.primitives.Longs;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yuanjiantao
 * @date Created in 2/28/2018
 */

@Getter
@Setter
public class HelloAckMessage extends BaseMessage {
    private HelloAckWraper helloAckWraper;

    public HelloAckMessage(byte[] encoded) {
        super(encoded);
        this.helloAckWraper = ProtoBufUtil.deserialize(encoded);
        this.cmd = MessageType.HELLO_ACK.getCode();
    }

    public HelloAckMessage(HelloAckWraper helloWraper) {
        this.helloAckWraper = helloWraper;
        this.encoded = ProtoBufUtil.serialize(helloWraper);
        this.cmd = MessageType.HELLO_ACK.getCode();
    }

    @Override
    public byte[] getEncoded() {
        return this.encoded;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }
}
