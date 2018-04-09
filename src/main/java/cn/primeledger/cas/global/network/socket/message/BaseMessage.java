package cn.primeledger.cas.global.network.socket.message;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * Base message is the parent class for all p2p messages.
 *
 * @author yuanjiantao
 * @date 2/26/2018
 */
@Data
@NoArgsConstructor
public abstract class BaseMessage implements Serializable {

    public boolean valid() {
        // todo baizhengwen add validation
        return true;
    }

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
