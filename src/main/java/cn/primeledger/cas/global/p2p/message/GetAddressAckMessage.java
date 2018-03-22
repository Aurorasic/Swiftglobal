package cn.primeledger.cas.global.p2p.message;

import cn.primeledger.cas.global.p2p.Peer;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class GetAddressAckMessage extends BaseMessage<GetAddressAckMessage.Wrapper> {

    private GetAddressAckMessage.Wrapper wrapper;

    public GetAddressAckMessage(byte[] encoded) {
        this((Wrapper) SerializationUtils.deserialize(encoded));
    }

    public GetAddressAckMessage(GetAddressAckMessage.Wrapper wrapper) {
        super(MessageType.GET_ADDRESS_ACK.getCode(), wrapper);
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Data
    @NoArgsConstructor
    @Slf4j
    public static class Wrapper implements Serializable {
        private List<Peer> peerList;
        private long nonce;
    }
}
