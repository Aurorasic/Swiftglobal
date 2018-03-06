package cn.primeledger.cas.global.p2p.message;

import lombok.Data;
import org.springframework.util.SerializationUtils;

/**
 * @author yuanjiantao
 * @date Created in 3/1/2018
 */

@Data
public class BizMessage extends BaseMessage {

    private BizWapper bizWapper;

    public BizMessage(BizWapper bizWapper) {
        this.bizWapper = bizWapper;
        this.encoded = SerializationUtils.serialize(bizWapper);
        this.cmd = MessageType.BIZ_MSG.getCode();
    }

    public BizMessage(byte[] encoded) {
        this.encoded = encoded;
        this.bizWapper = (BizWapper) SerializationUtils.deserialize(encoded);
        this.cmd = MessageType.BIZ_MSG.getCode();
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
