package cn.primeledger.cas.global.p2p.message;

import cn.primeledger.cas.global.utils.ProtoBufUtil;
import lombok.Data;

/**
 * @author yuanjiantao
 * @date Created in 2/27/2018
 */
@Data
public class HelloMessage extends BaseMessage {

    private HelloWraper helloWraper;

    public HelloMessage(byte[] encoded) {

        super(encoded);
        this.helloWraper = ProtoBufUtil.deserialize(encoded);
        this.cmd = MessageType.HELLO.getCode();
    }

    public HelloMessage(HelloWraper helloWraper) {
        this.helloWraper = helloWraper;
        this.encoded = ProtoBufUtil.serialize(helloWraper);
        this.cmd = MessageType.HELLO.getCode();
    }

    @Override
    public byte[] getEncoded() {
        return this.encoded;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return HelloAckMessage.class;
    }
}
