package com.higgsblock.global.chain.app.blockchain.transaction;

import com.alibaba.fastjson.annotation.JSONType;
import com.google.common.base.Objects;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * For checking a spending transaction that it can spend an out save without spending on the most long chain.
 *
 * @author yuguojia
 * @create 2018-02-23
 **/
@Data
@NoArgsConstructor
@JSONType(includes = {"hash", "index", "output", "address"})
public class UTXO extends BaseSerializer {

    /**
     * the transaction hash
     */
    private String hash;

    /**
     * the index of output in the tx bebin with 0
     */
    private short index;

    private TransactionOutput output;

    private String address;

    public UTXO(Transaction tx, short outIndex, TransactionOutput output) {
        this.hash = tx.getHash();
        this.index = outIndex;
        this.output = output;
        this.address = output.getLockScript().getAddress();
    }

    public static String buildKey(String hash, short index) {
        return hash + "_" + index;
    }

    public String getKey() {
        return buildKey(hash, index);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getIndex(), getHash());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UTXO other = (UTXO) obj;
        return getIndex() == other.getIndex() && getHash().equals(other.getHash());
    }

    public String getCurrency() {
        return output.getMoney().getCurrency();
    }

    public boolean isCASCurrency() {
        return output.isCASCurrency();
    }

    public boolean isMinerCurrency() {
        return output.isMinerCurrency();
    }

    public boolean hasMinerStake() {
        return output.hasMinerStake();
    }

    public boolean isCommunityCurrency() {
        return output.isCommunityCurrency();
    }

    public boolean isIssueTokenCurrency() {
        return output.isIssueTokenCurrency();
    }
}