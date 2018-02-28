package cn.primeledger.cas.global.script;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

/**
 * P2PKH or P2SH or multi-sig address script for out-put coin locking
 *
 * @author yuguojia
 * @create 2018-02-26
 **/
@Getter
@Setter
public class LockScript {
    /**
     * lock script type such as P2PKH or P2SH
     */
    private short type;
    private String address;

    public boolean valid() {
        if (StringUtils.isEmpty(address)) {
            return false;
        }
        return true;
    }
}