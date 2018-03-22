package cn.primeledger.cas.global.p2p.message;

import cn.primeledger.cas.global.crypto.ECKey;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;

/**
 * @author yuanjiantao
 * @date Created in 2/27/2018
 */
@Data
public class HelloMessage extends BaseMessage<HelloMessage.Wrapper> {

    public HelloMessage(byte[] encoded) {
        this((Wrapper) SerializationUtils.deserialize(encoded));
    }

    public HelloMessage(Wrapper wrapper) {
        super(MessageType.HELLO.getCode(), wrapper);
    }

    @Override
    public Class<?> getAnswerMessage() {
        return HelloAckMessage.class;
    }


    @Data
    @NoArgsConstructor
    @Slf4j
    public static class Wrapper implements Serializable {
        private String ip;
        private int port;
        private String pubKey;
        private String signature;

        public boolean invalidSignature() {
            return !ECKey.verifySign(ip, signature, pubKey);
        }

        public boolean invalidParams() {
            return StringUtils.isEmpty(ip)
                    || port == 0
                    || StringUtils.isEmpty(pubKey)
                    || StringUtils.isEmpty(signature);
        }
    }
}
