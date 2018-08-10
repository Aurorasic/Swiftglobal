package com.higgsblock.global.chain.app.blockchain.transaction;

import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.blockchain.script.UnLockScript;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * @author baizhengwen
 * @create 2018-03-06
 **/
@Data
@NoArgsConstructor
@JSONType(includes = {"prevOut", "unLockScript"})
public class TransactionInput extends BaseSerializer {
    /**
     * the sources of current spending
     */
    private TransactionOutPoint prevOut;

    /**
     * unlock script: signature and pk
     */
    private UnLockScript unLockScript;

    public String getHash() {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder()
                .append(function.hashString(null == prevOut ? StringUtils.EMPTY : prevOut.getHash(), Charsets.UTF_8))
                .append(function.hashString(null == unLockScript ? StringUtils.EMPTY : unLockScript.getHash(), Charsets.UTF_8));
        return function.hashString(builder, Charsets.UTF_8).toString();
    }

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