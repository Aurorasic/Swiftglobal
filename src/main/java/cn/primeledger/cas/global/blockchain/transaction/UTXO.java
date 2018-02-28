package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.entity.BaseSerializer;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * For checking a spending transaction that it can spend an out put without spending on the most long chain.
 *
 * @author yuguojia
 * @create 2018-02-23
 **/
@Setter
@Getter
public class UTXO extends BaseSerializer {

    /**
     * the transaction hash
     */
    private String hash;

    /**
     * the transaction type
     */
    private short type;

    /**
     * the index of output in the tx
     */
    private int index;

    /**
     * CAS amount
     */
    private BigDecimal amount;

    /**
     * There is not only cas coin, a different currency is a different token
     */
    private String currency;

    public UTXO(String hash,
                short type,
                int index,
                BigDecimal amount,
                String currency) {
        this.hash = hash;
        this.type = type;
        this.index = index;
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public String toString() {
        return String.format("utxo of : %s(%s:%d)", amount, hash, index);
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
}