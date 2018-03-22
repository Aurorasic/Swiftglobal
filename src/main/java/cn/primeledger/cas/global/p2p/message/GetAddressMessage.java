package cn.primeledger.cas.global.p2p.message;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhao xiaogang
 */

@Getter
@Setter
public class GetAddressMessage extends BaseMessage<GetAddressMessage.Wrapper> {

    public GetAddressMessage(byte[] encoded) {
        this((Wrapper) SerializationUtils.deserialize(encoded));
    }

    public GetAddressMessage(GetAddressMessage.Wrapper wrapper) {
        super(MessageType.GET_ADDRESS.getCode(), wrapper);
    }

    @Override
    public Class<?> getAnswerMessage() {
        return GetAddressAckMessage.class;
    }


    @Data
    @NoArgsConstructor
    @Slf4j
    public static class Wrapper implements Serializable {
        private List<String> addressList;
        private long nonce;
    }
}
