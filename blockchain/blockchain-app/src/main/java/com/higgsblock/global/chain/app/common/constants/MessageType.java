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
public enum MessageType {

    // ================ 000-099: base related =====================

    // ================ 200-299: block related ====================
    BLOCK("200"),
    INVENTORY("201"),

    MAX_HEIGHT_REQUEST("202"),
    MAX_HEIGHT_RESPONSE("203"),

    BLOCK_REQUEST("204"),
    BLOCK_RESPONSE("205"),

    SOURCE_BLOCK_REQUEST("206"),
    SOURCE_BLOCK_RESPONSE("207"),

    // ================ 300-399: transaction related ==============
    TRANSACTION("300"),

    // ================ 400-499: consensus related ================
    VOTE_TABLE("400");

    private String code;


    public static MessageType getByCode(String code) {
        for (MessageType item : values()) {
            if (StringUtils.equals(code, item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
