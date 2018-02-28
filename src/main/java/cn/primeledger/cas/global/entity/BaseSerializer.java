package cn.primeledger.cas.global.entity;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @author baizhengwen
 * @date Created in 2018/2/24
 */
public class BaseSerializer implements Serializable {

    private static final long serialVersionUID = 6040573625894308118L;

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
