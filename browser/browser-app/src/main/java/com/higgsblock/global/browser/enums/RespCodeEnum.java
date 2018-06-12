package com.higgsblock.global.browser.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yangshenghong
 * @date 2018-05-24
 */
@Getter
@AllArgsConstructor
public enum RespCodeEnum {
    /**
     * status code
     */
    SUCCESS("000", "success"),
    PARAMETER_ERROR("103", "Parameters of the abnormal"),
    PATH_ERROR("101", "Request path exception"),
    BUSINESS_ERROR("201", "Business exceptions"),
    DATA_ERROR("202", "Business exceptions"),
    SYS_ERROR("500", "system error");
    private String code;
    private String desc;
}
