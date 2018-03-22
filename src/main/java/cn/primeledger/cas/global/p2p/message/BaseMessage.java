package cn.primeledger.cas.global.p2p.message;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;

/**
 * @author yuanjiantao
 * @date 2/26/2018
 */
@Data
@NoArgsConstructor
public abstract class BaseMessage<T extends Serializable> {

    protected int cmd;
    protected T data;

    public BaseMessage(int cmd, T data) {
        this.cmd = cmd;
        this.data = data;
    }

    protected static <T> T parse(byte[] bytes) {
        return (T) SerializationUtils.deserialize(bytes);
    }

    /**
     * get encoded
     *
     * @return
     */
    public byte[] getEncoded() {
        return SerializationUtils.serialize(data);
    }

    protected boolean closeAfterSend() {
        return false;
    }

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

}
