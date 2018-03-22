package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.entity.BaseSerializer;
import cn.primeledger.cas.global.script.LockScript;
import cn.primeledger.cas.global.utils.AmountUtils;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.math.BigDecimal;

/**
 * @author baizhengwen
 * @create 2018-03-06
 **/
@Data
public class TransactionOutput extends BaseSerializer {

    /**
     * the amount of cas coin to spend
     */
    private BigDecimal amount;

    /**
     * There is not only cas coin, a different currency is a different token.
     * If it is null, the coin is CAS.
     */
    private String currency;

    /**
     * locking script, it could be public key hash or p2sh and so on
     */
    private LockScript lockScript;

    public boolean valid() {
        if (!AmountUtils.check(false, amount)) {
            return false;
        }
        if (null == lockScript) {
            return false;
        }
        return lockScript.valid();
    }

    public String getHash() {
        StringBuilder builder = new StringBuilder();
        if (amount != null) {
            builder.append(Hashing.sha256().hashString(amount.toPlainString(), Charsets.UTF_8));
        }
        builder.append(Hashing.sha256().hashString(null == currency ? Strings.EMPTY : currency, Charsets.UTF_8));
        return builder.toString();
    }

    public boolean isCASCurrency() {
        if (StringUtils.equals(SystemCurrencyEnum.CAS.getCurrency(), currency)) {
            return true;
        }
        return false;
    }

    public boolean isMinerCurrency() {
        if (StringUtils.equals(SystemCurrencyEnum.MINER.getCurrency(), currency)) {
            return true;
        }
        return false;
    }

    @JSONField(serialize = false)
    public boolean hasMinerStake() {
        if (isMinerCurrency() &&
                amount.compareTo(new BigDecimal(1)) >= 0) {
            return true;
        }
        return false;
    }

    public boolean isCommunityManagerCurrency() {
        if (StringUtils.equals(SystemCurrencyEnum.COMMUNITY_MANAGER.getCurrency(), currency)) {
            return true;
        }
        return false;
    }

    public boolean isIssueTokenCurrency() {
        if (StringUtils.equals(SystemCurrencyEnum.ISSUE_TOKEN.getCurrency(), currency)) {
            return true;
        }
        return false;
    }

    @JSONField(serialize = false)
    public boolean hasIssueTokenStake() {
        if (isMinerCurrency() &&
                amount.compareTo(BigDecimal.ONE) >= 0) {
            return true;
        }
        return false;
    }
}