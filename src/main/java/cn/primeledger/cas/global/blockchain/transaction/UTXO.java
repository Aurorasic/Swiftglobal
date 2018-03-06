package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.entity.BaseSerializer;
import com.google.common.base.Objects;
import lombok.Data;

/**
 * For checking a spending transaction that it can spend an out put without spending on the most long chain.
 *
 * @author yuguojia
 * @create 2018-02-23
 **/
@Data
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
    private short index;

    private BaseOutput output;

    private String address;


    public UTXO() {
    }

    public UTXO(InputOutputTx tx, short outIndex, BaseOutput output) {
        this.hash = tx.getHash();
        this.type = tx.getType();
        this.index = outIndex;
        this.output = output;
        this.address = output.getLockScript().getAddress();
    }

    public String getKey() {
        return buildKey(hash, index);
    }

    public static String buildKey(String hash, short index) {
        return hash + "_" + index;
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