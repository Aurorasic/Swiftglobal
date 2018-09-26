package com.higgsblock.global.chain.app.blockchain.transaction;

import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import com.higgsblock.global.chain.common.utils.Money;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

/**
 * @author yuguojia
 * @create 2018-02-24
 **/
@Setter
@Getter
@NoArgsConstructor
@JSONType(includes = {"transactionHash", "index", "output"})
public class TransactionOutPoint extends BaseSerializer {
    /**
     * the hash of source transaction for spending
     */
    private String transactionHash;

    /**
     * the index out of source transaction
     */
    private short index;

    private TransactionOutput output;

    public String getHash() {
        HashFunction function = Hashing.sha256();
        StringBuilder builder = new StringBuilder()
                .append(function.hashString(null == transactionHash ? StringUtils.EMPTY : transactionHash, Charsets.UTF_8))
                .append(function.hashInt(index));
        return function.hashString(builder, Charsets.UTF_8).toString();
    }

    public boolean valid() {
        if (StringUtils.isEmpty(transactionHash)) {
            return false;
        }
        if (index < 0) {
            return false;
        }
        return true;
    }

    public String getKey() {
        return transactionHash + "_" + index;
    }

    public String getCurrency() {
        return output.getMoney().getCurrency();
    }

    public Money getMoney() {
        return output.getMoney();
    }

    public String getAddress() {
        return output.getLockScript().getAddress();
    }
}