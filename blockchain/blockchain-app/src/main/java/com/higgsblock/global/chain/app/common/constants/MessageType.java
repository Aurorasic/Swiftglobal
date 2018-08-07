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
    HELLO("001"),
    HELLO_ACK("002"),
    SYNC_PEERS("003"),


    // ================ 200-299: block related ====================
    BLOCK("200"),
    INVENTORY("201"),
    BLOCK_REQUEST("202"),
    BLOCK_RESPONSE("203"),

    // ================ 300-399: transaction related ==============
    TRANSACTION("300"),

    // ================ 400-499: consensus related ================
    VOTE_TABLE("400"),
    ORIGINAL_BLOCK("401"),

    // ================ 500-599: request and response =============
    MAX_HEIGHT_REQUEST("501"),
    MAX_HEIGHT_RESPONSE("502"),

    VOTING_BLOCK_REQUEST("505"),
    VOTING_BLOCK_RESPONSE("506");

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
