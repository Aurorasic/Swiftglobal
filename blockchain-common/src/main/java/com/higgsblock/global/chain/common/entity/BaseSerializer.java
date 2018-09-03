package com.higgsblock.global.chain.common.entity;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @author baizhengwen
 * @date 2018-04-27
 */
public abstract class BaseSerializer implements Serializable {

    private static final long serialVersionUID = 6040573625894308118L;

    @Override
    public String toString() {
        try {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE, true);
        } catch (Exception e) {
            // ignore
            return super.toString();
        }
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }
}
