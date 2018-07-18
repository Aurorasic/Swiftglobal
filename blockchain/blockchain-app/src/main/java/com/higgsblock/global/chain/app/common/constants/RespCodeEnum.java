package com.higgsblock.global.chain.app.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

/**
 * @author baizhengwen
 * @date 2018/3/16
 */
@Getter
@AllArgsConstructor
public enum RespCodeEnum {

    //
    SUCCESS("000", "success"),

    FAILED("200", "failed"),

    //
    PARAM_INVALID("100", "param invalid"),
    HASH_NOT_EXIST("101", "hash don't exist!"),

    //
    SYS_ERROR("500", "system error");

    private String code;
    private String desc;

    public static RespCodeEnum getByCode(String code) {
        for (RespCodeEnum item : values()) {
            if (StringUtils.equals(code, item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
