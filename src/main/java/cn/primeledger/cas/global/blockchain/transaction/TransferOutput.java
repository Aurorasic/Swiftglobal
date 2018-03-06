package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.utils.AmountUtils;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;

import java.math.BigDecimal;

/**
 * @author yuguojia
 * @create 2018-02-22
 **/
@Getter
@Setter
@NoArgsConstructor
public class TransferOutput extends BaseOutput {

    /**
     * the amount of cas coin to spend
     */
    private BigDecimal amount;

    /**
     * There is not only cas coin, a different currency is a different token.
     * If it is null, the coin is CAS.
     */
    private String currency;

    @Override
    public String getHash() {
        StringBuilder builder = new StringBuilder();
        if (amount != null) {
            builder.append(Hashing.sha256().hashString(amount.toPlainString(), Charsets.UTF_8));
        }
        builder.append(Hashing.sha256().hashString(null == currency ? Strings.EMPTY : currency, Charsets.UTF_8));
        return builder.toString();
    }

    @Override
    public boolean valid() {
        if (!AmountUtils.check(false, amount)) {
            return false;
        }
        return super.valid();
    }
}