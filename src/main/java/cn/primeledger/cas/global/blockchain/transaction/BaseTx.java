package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.entity.BaseSerializer;
import lombok.Data;

/**
 * @author baizhengwen
 * @create 2018-03-06
 */
@Data
public abstract class BaseTx extends BaseSerializer {
    /**
     * the hash of this transaction
     */
    protected String hash;

    /**
     * the version of CAS global
     */
    protected short version;

    /**
     * the transaction type
     */
    protected short type;

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
    protected String sign;

    public abstract String getHash();

    public boolean isSysTransaction() {
        return TransactionTypeEnum.isSystem(type);
    }
}
