package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.entity.BaseSerializer;
import cn.primeledger.cas.global.script.UnLockScript;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yuguojia
 * @create 2018-02-22
 **/
@Setter
@Getter
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

        return true;
    }
}