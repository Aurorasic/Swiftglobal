package cn.primeledger.cas.global.p2p.message;

import cn.primeledger.cas.global.p2p.exception.MessageDecodeException;

/**
 * @author yuanjiantao
 * @date 2/26/2018
 */
public class MessageFactory {

    public BaseMessage create(int code, byte[] encoded) throws Exception {

        MessageType c = MessageType.of(code);
        if (code == 0) {
            throw new Exception("Invalid message code: " + code);
        }

        try {
            switch (c) {
                case HELLO:
                    return new HelloMessage(encoded);
                case HELLO_ACK:
                    return new HelloAckMessage(encoded);
                case PEERS:
                    return new PeersMessage(encoded);
                case GET_PEERS:
                    return new GetPeersMessage();
                case BIZ_MSG:
                    return new BizMessage(encoded);
                default:
                    throw new MessageDecodeException("The message not support");
            }
        } catch (Exception e) {
            throw new MessageDecodeException("Failed to decode message");
        }
    }
}
