package cn.primeledger.cas.global.p2p.message;

import cn.primeledger.cas.global.crypto.ECKey;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;

/**
 * @author yuanjiantao
 * @date Created in 2/28/2018
 */

@Getter
@Setter
public class HelloAckMessage extends BaseMessage<HelloAckMessage.Wrapper> {

    public HelloAckMessage(byte[] encoded) {
        this((Wrapper) SerializationUtils.deserialize(encoded));
    }

    public HelloAckMessage(Wrapper wrapper) {
        super(MessageType.HELLO_ACK.getCode(), wrapper);
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }


    @Data
    @NoArgsConstructor
    public static class Wrapper implements Serializable {
        private String ip;
        private int port;
        private String pubKey;
        private String signature;

        public boolean validSignature() {
            return ECKey.verifySign(ip, signature, pubKey);
        }

        public boolean validParams() {
            return StringUtils.isNotEmpty(ip)
                    && port != 0
                    && StringUtils.isNotEmpty(pubKey)
                    && StringUtils.isNotEmpty(signature);
        }
    }

}
