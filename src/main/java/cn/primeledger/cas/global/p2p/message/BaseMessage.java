package cn.primeledger.cas.global.p2p.message;

import lombok.NoArgsConstructor;

/**
 * @author yuanjiantao
 * @date 2/26/2018
 */
@NoArgsConstructor
public abstract class BaseMessage {

    protected final short magicNumber = 12345;
    //    protected short version;
    /**
     *
     */
    protected int cmd;
    //    protected byte[] checksum;
    /**
     *
     */
    protected byte[] encoded;

    public BaseMessage(byte[] encoded) {
        this.encoded = encoded;
    }

    /**
     * get encoded
     *
     * @return
     */
    public abstract byte[] getEncoded();

    /**
     * return answer message
     *
     * @return
     */
    public abstract Class<?> getAnswerMessage();

    /**
     * toString
     *
     * @return
     */
    @Override
    public abstract String toString();

    public int getCmd() {
        return cmd;
    }

}
