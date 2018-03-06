package cn.primeledger.cas.global.p2p.message;

/**The Error code for the disconnection reasons
 *
 * @author zhao xiaogang
 */
public enum  ErrorCode {
    CONNECTED(0x01);

    private int code;
    ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public byte toByte() {
        return (byte) code;
    }
}
