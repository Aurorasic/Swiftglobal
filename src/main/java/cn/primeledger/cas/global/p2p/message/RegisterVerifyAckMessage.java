package cn.primeledger.cas.global.p2p.message;

import cn.primeledger.cas.global.utils.ProtoBufUtil;
import lombok.Data;

/**
 * @author yuanjiantao
 * @date Created in 3/5/2018
 */
@Data
public class RegisterVerifyAckMessage extends BaseMessage {

    private String signature;

    public RegisterVerifyAckMessage(String signature) {
        this.signature = signature;
        this.encoded = ProtoBufUtil.serialize(signature);
        this.cmd = MessageType.REGISTERVERIFYACK.getCode();
    }

    public RegisterVerifyAckMessage(byte[] encoded) {
        this.encoded = encoded;
        this.cmd = MessageType.REGISTERVERIFYACK.getCode();
        this.signature = ProtoBufUtil.deserialize(encoded);
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
