package cn.primeledger.cas.global.constants;

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

    // base messages
    PINT((short) 0, (short) 1, null),

    // business messages
    TRANSACTION_BROADCAST((short) 100, (short) 1, "transferTxHandler"),

    BLOCK_BROADCAST((short) 200, (short) 1, "blockHandler"),

    SIGN_BLOCK((short) 201, (short) 1, "collectSignHandler");

    private short type;
    private short version;
    private String handlerName;

    public String getCode() {
        return type + "_" + version;
    }

    public static EntityType getTypeAndVersion(short type, short version) {
        return getByCode(type + "_" + version);
    }

    public static EntityType getByCode(String code) {
        for (EntityType item : values()) {
            if (StringUtils.equals(code, item.getCode())) {
                return item;
            }
        }
        return null;
    }
}
