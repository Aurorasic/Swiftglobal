package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.blockchain.PubKeyAndSignPair;
import cn.primeledger.cas.global.entity.BaseBizEntity;
import cn.primeledger.cas.global.utils.JsonSizeCounter;
import cn.primeledger.cas.global.utils.SizeCounter;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.List;

/**
 * @author yuguojia
 * @date 2018/03/08
 **/
@Data
public class Transaction extends BaseBizEntity {

    private static final int LIMITED_SIZE_UNIT = 1024 * 100;

    /**
     * the hash of this transaction
     */
    protected String hash;
    /**
     * lock after pointed block height of time
     */
    protected long lockTime;
    /**
     * extra info for this transaction
     */
    protected String extra;
    /**
     * sign of this transaction
     */
    protected PubKeyAndSignPair pubKeyAndSignPair;
    /**
     * the sources of current spending
     */
    private List<TransactionInput> inputs;
    /**
     * transfer to other coin
     */
    private List<TransactionOutput> outputs;

    /**
     * the timestamp of this transaction created
     */
    private long transactionTime = System.currentTimeMillis();

    private String minerPubKey;

    public String getHash() {
        if (StringUtils.isBlank(hash)) {
            HashFunction function = Hashing.sha256();
            StringBuilder builder = new StringBuilder();
            builder.append(function.hashInt(version));
            builder.append(function.hashLong(transactionTime));
            builder.append(function.hashLong(lockTime));
            builder.append(function.hashString(null == extra ? Strings.EMPTY : extra, Charsets.UTF_8));
            builder.append(function.hashString(null == minerPubKey ? Strings.EMPTY : minerPubKey, Charsets.UTF_8));
            if (CollectionUtils.isNotEmpty(inputs)) {
                inputs.forEach((input) -> {
                    TransactionOutPoint prevOut = input.getPrevOut();
                    if (prevOut != null) {
                        builder.append(function.hashLong(prevOut.getIndex()));
                        String prevOutHash = prevOut.getHash();
                        builder.append(function.hashString(null == prevOutHash ? Strings.EMPTY : prevOutHash, Charsets.UTF_8));
                    }
                });
            } else {
                builder.append(function.hashInt(0));
            }
            if (CollectionUtils.isNotEmpty(outputs)) {
                outputs.forEach((output) -> builder.append(output.getHash()));
            } else {
                builder.append(function.hashInt(0));
            }
            hash = function.hashString(builder, Charsets.UTF_8).toString();
        }
        return hash;
    }

    public TransactionOutput getTransactionOutputByIndex(short index) {
        int size = outputs.size();
        if (size <= index + 1 || index < 0) {
            return null;
        }
        return outputs.get(index);
    }

    public boolean sizeAllowed() {
        SizeCounter sizeCounter = new JsonSizeCounter();
        if (sizeCounter.calculateSize(this.inputs) > LIMITED_SIZE_UNIT) {
            return false;
        }
        if (sizeCounter.calculateSize(this.outputs) > LIMITED_SIZE_UNIT) {
            return false;
        }
        if (sizeCounter.calculateSize(this.extra) > LIMITED_SIZE_UNIT) {
            return false;
        }
        if (sizeCounter.calculateSize(this) > LIMITED_SIZE_UNIT) {
            return false;
        }
        return true;
    }
}