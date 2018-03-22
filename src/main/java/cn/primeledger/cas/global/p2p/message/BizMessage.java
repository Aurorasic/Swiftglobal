package cn.primeledger.cas.global.p2p.message;

import lombok.Data;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;

/**
 * @author yuanjiantao
 * @date Created in 3/1/2018
 */

@Data
public class BizMessage extends BaseMessage<BizMessage.Wrapper> {

    public BizMessage(Wrapper wrapper) {
        super(MessageType.BIZ_MSG.getCode(), wrapper);
    }

    public BizMessage(byte[] encoded) {
        this((Wrapper) SerializationUtils.deserialize(encoded));
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Data
    public static class Wrapper implements Serializable {
        private short type;

        private short version;

        private String data;

    }


}
