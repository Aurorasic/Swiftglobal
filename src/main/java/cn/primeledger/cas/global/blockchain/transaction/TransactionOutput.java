package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.entity.BaseSerializer;
import cn.primeledger.cas.global.script.LockScript;
import cn.primeledger.cas.global.utils.AmountUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author yuguojia
 * @create 2018-02-22
 **/
@Getter
@Setter
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
        if (!AmountUtils.check(false, amount) || !lockScript.valid()) {
            return false;
        }
        return true;
    }
}