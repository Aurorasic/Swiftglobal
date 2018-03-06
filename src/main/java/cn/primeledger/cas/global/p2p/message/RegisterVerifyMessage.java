package cn.primeledger.cas.global.p2p.message;

import cn.primeledger.cas.global.utils.ProtoBufUtil;
import lombok.Data;

/**
 * @author yuanjiantao
 * @date Created in 3/5/2018
 */
@Data
public class RegisterVerifyMessage extends BaseMessage {


    private RegisterVerifyWrapper registerVerifyWrapper;

    public RegisterVerifyMessage(RegisterVerifyWrapper registerVerifyWrapper) {
        this.registerVerifyWrapper = registerVerifyWrapper;
        this.encoded = ProtoBufUtil.serialize(registerVerifyWrapper);
        this.cmd = MessageType.REGISTERVERIFY.getCode();
    }

    public RegisterVerifyMessage(byte[] encoded) {
        this.registerVerifyWrapper = ProtoBufUtil.deserialize(encoded);
        this.encoded = encoded;
        this.cmd = MessageType.REGISTERVERIFY.getCode();
    }


    @Override
    public byte[] getEncoded() {
        return encoded;
    }

    @Override
    public Class<?> getAnswerMessage() {
        return RegisterVerifyAckMessage.class;
    }
}
