package cn.primeledger.cas.global.p2p.message;

/**
 * enum for sorts of messages
 *
 * @author yuanjiantao
 * @date 2/26/2018
 */
public enum MessageType {

    /**
     * PINGMESSAGE
     */
    PING(1),
    /**
     * PONGMESSAGE
     */
    PONG(2),

    /**
     * HelloMessage
     */
    HELLO(3);


    private static final MessageType[] MAP = new MessageType[256];

    static {
        for (MessageType mc : MessageType.values()) {
            MAP[mc.code] = mc;
        }
    }

    public static MessageType of(int code) {
        return MAP[code];
    }

    private int code;

    MessageType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}

