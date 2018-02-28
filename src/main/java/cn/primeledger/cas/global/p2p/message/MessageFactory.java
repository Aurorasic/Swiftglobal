package cn.primeledger.cas.global.p2p.message;

/**
 * 
 *
 * @author yuanjiantao
 * @date  2/26/2018
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
                    return new PingMessage(encoded);
                case PONG:
                    return new PongMessage(encoded);

                default:
                    throw new Exception();
            }
        } catch (Exception e) {
            throw new Exception("Failed to decode message", e);
        }
    }
}
