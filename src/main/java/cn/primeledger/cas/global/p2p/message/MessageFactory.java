package cn.primeledger.cas.global.p2p.message;

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
                case PING:
                    return new PingMessage();
                case PONG:
                    return new PongMessage(encoded);
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
                case REGISTER:
                    return new RegisterMessage(encoded);
                case REGISTERVERIFY:
                    return new RegisterVerifyMessage(encoded);
                case REGISTERVERIFYACK:
                    return new RegisterVerifyAckMessage(encoded);
                default:
                    throw new Exception();
            }
        } catch (Exception e) {
            throw new Exception("Failed to decode message", e);
        }
    }
}
