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
    HELLO(3),

    HELLO_ACK(4),

    /**
     * get nodes
     */
    GET_PEERS(5),

    /**
     * nodes
     */
    PEERS(6),

    /**
     * Business message
     */
    BIZ_MSG(7),

    /**
     * register center register
     */
    REG(8),

    /**
     * register center verify
     */
    REG_ACK(9),

    /**
     * register center verify ack
     */
    REG_RESEND(10),

    INIT_PEERS(11),

    INIT_PEERS_ACK(12),

    GET_ADDRESS(13),

    GET_ADDRESS_ACK(14);

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

