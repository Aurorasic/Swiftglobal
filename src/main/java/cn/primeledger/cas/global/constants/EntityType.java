package cn.primeledger.cas.global.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * message type enum
 *
 * @author baizhengwen
 * @date 2018/2/28
 */
@Getter
@AllArgsConstructor
public enum EntityType {

    // ================ type 0-99: p2p related =========================
    PINT((short) 0),

    // ================ type 200-299: block related ====================
    BLOCK_BROADCAST((short) 200),
    INVENTORY((short) 201),
    MAXHEIGHT((short) 202),

    // ================ type 300-399: transaction related ==============
    TRANSACTION_TRANSFER_BROADCAST((short) 300),

    TRANSACTION_MINER_MANAGE_BROADCAST((short) 301),

    // ================ type 400-499: consensus related ================
    SIGN_BLOCK((short) 400),

    BLOCK_COLLECT_SIGN((short) 401),

    BLOCK_CREATE_SIGN((short) 402);

    private short code;


    public static EntityType getByCode(short code) {
        for (EntityType item : values()) {
            if (code == item.getCode()) {
                return item;
            }
        }
        return null;
    }
}
