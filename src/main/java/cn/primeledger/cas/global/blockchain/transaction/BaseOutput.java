package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.entity.BaseSerializer;
import cn.primeledger.cas.global.script.LockScript;
import lombok.Data;

/**
 * @author baizhengwen
 * @create 2018-03-06
 **/
@Data
public abstract class BaseOutput extends BaseSerializer {

    /**
     * locking script, it could be public key hash or p2sh and so on
     */
    private LockScript lockScript;

    public abstract String getHash();

    public boolean valid() {
        if (null == lockScript) {
            return false;
        }
        return lockScript.valid();
    }
}