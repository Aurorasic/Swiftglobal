package cn.primeledger.cas.global.blockchain.transaction;

import lombok.Getter;

/**
 * @author yuguojia
 * @create 2018-02-26
 **/
@Getter
public enum ScriptTypeEnum {

    /**
     * pay to public key
     */
    P2PK((short) 0),

    /**
     * pay to public key hash
     */
    P2PKH((short) 1),

    /**
     * pay to script hash
     */
    P2SH((short) 2);

    /**
     * lock script type such as P2PKH or P2SH
     */
    private short type;

    ScriptTypeEnum(short type) {
        this.type = type;
    }

    public static boolean containType(short type) {
        ScriptTypeEnum[] values = ScriptTypeEnum.values();
        for (ScriptTypeEnum typeEnum : values) {
            if (type == typeEnum.getType()) {
                return true;
            }
        }
        return false;
    }
}