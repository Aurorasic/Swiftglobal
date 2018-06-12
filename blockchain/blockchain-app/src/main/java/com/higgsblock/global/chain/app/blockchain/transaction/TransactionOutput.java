package com.higgsblock.global.chain.app.blockchain.transaction;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.app.script.LockScript;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.common.enums.SystemCurrencyEnum;
import com.higgsblock.global.chain.common.utils.Money;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.nio.charset.Charset;

/**
 * @author baizhengwen
 * @create 2018-03-06
 **/
@Data
@NoArgsConstructor
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
        StringBuilder builder = new StringBuilder();
        if (money.getValue() != null) {
            builder.append(Hashing.sha256().hashString(money.getValue(), Charsets.UTF_8));
        }
        builder.append(Hashing.sha256().hashString(null == money.getCurrency() ? Strings.EMPTY : money.getCurrency(), Charsets.UTF_8));
        String hash = Hashing.sha256().hashString(builder.toString(), Charset.forName("UTF-8")).toString();
        return hash;
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

    @JSONField(serialize = false)
    public boolean hasMinerStake() {
        if (isMinerCurrency() &&
                money.compareTo(new Money("1", SystemCurrencyEnum.MINER.getCurrency())) >= 0) {
            return true;
        }
        return false;
    }

    public boolean isCommunityManagerCurrency() {
        if (StringUtils.equals(SystemCurrencyEnum.COMMUNITY_MANAGER.getCurrency(), money.getCurrency())) {
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

    @JSONField(serialize = false)
    public boolean hasIssueTokenStake() {
        if (isMinerCurrency() &&
                money.compareTo(new Money("1", SystemCurrencyEnum.ISSUE_TOKEN.getCurrency())) >= 0) {
            return true;
        }
        return false;
    }
}