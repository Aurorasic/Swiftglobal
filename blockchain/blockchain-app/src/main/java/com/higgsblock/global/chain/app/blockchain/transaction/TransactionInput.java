package com.higgsblock.global.chain.app.blockchain.transaction;

import com.higgsblock.global.chain.app.script.UnLockScript;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
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