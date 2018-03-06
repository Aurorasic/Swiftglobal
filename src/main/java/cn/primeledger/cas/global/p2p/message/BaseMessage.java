package cn.primeledger.cas.global.p2p.message;

import lombok.NoArgsConstructor;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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

    @Override
    public String toString() {
        try {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        } catch (Exception e) {
            // ignore
            return super.toString();
        }
    }

    public int getCmd() {
        return cmd;
    }

}
