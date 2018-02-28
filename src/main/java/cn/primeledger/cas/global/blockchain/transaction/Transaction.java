package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.entity.BaseSerializer;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yuguojia
 * @create 2018-02-22
 **/
@Getter
@Setter
public class Transaction extends BaseSerializer {
    /**
     * the version of CAS global
     */
    private short version;

    /**
     * the transaction type
     */
    private short type;

    /**
     * the sources of current spending
     */
    private List<TransactionInput> inputs;

    /**
     * transfer to other coin
     */
    private List<TransactionOutput> outputs;

    /**
     * lock after pointed block height of time
     */
    private long lockTime;

    /**
     * extra info for this transaction
     */
    private String extra;

    /**
     * the hash of this transaction
     */
    private transient String hash;


    public String getHash() {
        if (StringUtils.isBlank(hash)) {
            HashFunction function = Hashing.sha256();
            StringBuilder builder = new StringBuilder();
            builder.append(function.hashInt(version));
            builder.append(function.hashInt(type));
            builder.append(function.hashLong(lockTime));
            builder.append(function.hashString(null == extra ? Strings.EMPTY : extra, Charsets.UTF_8));
            if (CollectionUtils.isNotEmpty(inputs)) {
                inputs.forEach((input) -> {
                    TransactionOutPoint prevOut = input.getPrevOut();
                    if (prevOut != null) {
                        String prevOutHash = prevOut.getHash();
                        int index = prevOut.getIndex();
                        builder.append(function.hashLong(index));
                        builder.append(function.hashString(null == prevOutHash ? Strings.EMPTY : prevOutHash, Charsets.UTF_8));
                    }
                });
            }
            if (CollectionUtils.isNotEmpty(outputs)) {
                outputs.forEach((output) -> {
                    BigDecimal amount = output.getAmount();
                    String currency = output.getCurrency();
                    if (amount != null) {
                        builder.append(function.hashString(amount.toPlainString(), Charsets.UTF_8));
                    }
                    builder.append(function.hashString(null == currency ? Strings.EMPTY : currency, Charsets.UTF_8));
                });
            }
            hash = function.hashString(builder, Charsets.UTF_8).toString();
        }
        return hash;
    }

    public boolean valid() {
        if (version < 0) {
            return false;
        }

        if (!TransactionTypeEnum.containType(type)) {
            return false;
        }

        if (type == TransactionTypeEnum.TRANSFER.getType()) {
            if (CollectionUtils.isEmpty(inputs) || CollectionUtils.isEmpty(outputs)) {
                return false;
            }
            if (StringUtils.isNotEmpty(extra)) {
                return false;
            }
            for (TransactionInput input : inputs) {
                if (!input.valid()) {
                    return false;
                }
            }
            for (TransactionOutput output : outputs) {
                if (!output.valid()) {
                    return false;
                }
            }
            //TODO check amount
        } else if (type == TransactionTypeEnum.TRANSFER_EXTRA.getType()) {
            //TODO
        } else {
            //TODO
        }
        return true;
    }
}