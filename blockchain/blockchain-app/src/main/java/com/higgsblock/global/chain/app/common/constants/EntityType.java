package com.higgsblock.global.chain.app.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

/**
 * message type enum
 *
 * @author baizhengwen
 * @date 2018/2/28
 */
@Getter
@AllArgsConstructor
public enum EntityType {

    // ================ type 000-099: base related =========================
    UNKNOWN("000"),
    SYNC_FRAMEWORK("010"),

    // ================ type 200-299: block related ====================
    BLOCK("200"),
    INVENTORY_NOTIFY("201"),
    MAX_HEIGHT_RESPONSE("202"),
    GET_BLOCK_REQUEST("203"),
    MAX_HEIGHT_REQUEST("204"),
    SOURCE_BLOCK_RESPONSE("205"),
    SOURCE_BLOCK_REQUEST("207"),
    BLOCK_RESPONSE("208"),

    // ================ type 300-399: transaction related ==============
    TRANSACTION("300"),

    // ================ type 400-499: consensus related ================
    VOTES_NOTIFY("400");

    private String code;


    public static EntityType getByCode(String code) {
        for (EntityType item : values()) {
            if (StringUtils.equals(code, item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
