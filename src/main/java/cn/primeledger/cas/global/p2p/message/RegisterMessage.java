package cn.primeledger.cas.global.p2p.message;

import cn.primeledger.cas.global.utils.ProtoBufUtil;
import lombok.Data;

/**
 * @author yuanjiantao
 * @date Created in 3/5/2018
 */
@Data
public class RegisterMessage extends BaseMessage {

    private RegisterWrapper registerWrapper;

    public RegisterMessage(RegisterWrapper registerWrapper) {
        this.registerWrapper = registerWrapper;
        this.encoded = ProtoBufUtil.serialize(registerWrapper);
        this.cmd = MessageType.REGISTER.getCode();
    }

    public RegisterMessage(byte[] encoded) {
        super(encoded);
        this.registerWrapper = ProtoBufUtil.deserialize(encoded);
        this.cmd = MessageType.REGISTER.getCode();
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
