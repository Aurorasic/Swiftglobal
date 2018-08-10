package com.higgsblock.global.chain.app.blockchain.transaction;

import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.Money;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * @author baizhengwen
 * @create 2018-03-06
 **/
@Data
@NoArgsConstructor
@JSONType(includes = {"money", "lockScript"})
public class TransactionOutput extends BaseSerializer {

    /**
     * the money of cas coin to spend
     * There is not only cas coin, a different currency is a different token.
     * If it is null, the coin is CAS.
     */
    private Money money;

    /**
     * locking script, it could be public key hash or p2sh and so on
     */
    private LockScript lockScript;

    public boolean valid() {
        if (!money.checkRange()) {
            return false;
        }
        if (null == lockScript) {
            return false;
        }
        return lockScript.valid();
    }

    public String getHash() {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder()
                .append(function.hashString(null == money ? StringUtils.EMPTY : money.getHash(), Charsets.UTF_8))
                .append(function.hashString(null == lockScript ? StringUtils.EMPTY : lockScript.getHash(), Charsets.UTF_8));
        return function.hashString(builder, Charsets.UTF_8).toString();
    }

    public boolean isCASCurrency() {
        if (StringUtils.equals(SystemCurrencyEnum.CAS.getCurrency(), money.getCurrency())) {
            return true;
        }
        return false;
    }

    public boolean isMinerCurrency() {
        if (StringUtils.equals(SystemCurrencyEnum.MINER.getCurrency(), money.getCurrency())) {
            return true;
        }
        return false;
    }

    public boolean hasMinerStake() {
        if (isMinerCurrency() &&
                money.compareTo(new Money("1", SystemCurrencyEnum.MINER.getCurrency())) >= 0) {
            return true;
        }
        return false;
    }

    public boolean isCommunityCurrency() {
        if (StringUtils.equals(SystemCurrencyEnum.COMMUNITY.getCurrency(), money.getCurrency())) {
            return true;
        }
        return false;
    }

    public boolean isIssueTokenCurrency() {
        if (StringUtils.equals(SystemCurrencyEnum.ISSUE_TOKEN.getCurrency(), money.getCurrency())) {
            return true;
        }
        return false;
    }

}