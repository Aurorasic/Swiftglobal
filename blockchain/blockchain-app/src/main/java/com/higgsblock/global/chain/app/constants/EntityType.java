package com.higgsblock.global.chain.app.constants;

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
    INVENTORY("201"),
    MAX_HEIGHT("202"),
    GET_BLOCK("203"),
    GET_MAX_HEIGHT("204"),
    SOURCE_BLOCK("205"),
    CANDIDATE_BLOCK("206"),
    RECOMMEND_BLOCK("207"),
    CANDIDATE_BLOCK_HASH("208"),
    QUERY_CANDIDATE_BLOCK("209"),
    QUERY_CANDIDATE_BLOCK_HASH("210"),

    // ================ type 300-399: transaction related ==============
    TRANSACTION("300"),

    // ================ type 400-499: consensus related ================
    ALLVOTE("400"),
    INITVOTE("401"),;

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
