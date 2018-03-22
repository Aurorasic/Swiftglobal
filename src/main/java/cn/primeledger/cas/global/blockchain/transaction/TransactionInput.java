package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.entity.BaseSerializer;
import cn.primeledger.cas.global.script.UnLockScript;
import lombok.Data;

/**
 * @author baizhengwen
 * @create 2018-03-06
 **/
@Data
public class TransactionInput extends BaseSerializer {
    /**
     * the sources of current spending
     */
    private TransactionOutPoint prevOut;

    /**
     * unlock script: signature and pk
     */
    private UnLockScript unLockScript;

    public boolean valid() {
        if (prevOut == null || unLockScript == null) {
            return false;
        }
        return prevOut.valid() && unLockScript.valid();
    }

    public String getPreUTXOKey() {
        return prevOut.getKey();
    }
}