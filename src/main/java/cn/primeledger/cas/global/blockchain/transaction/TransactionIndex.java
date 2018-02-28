package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.entity.BaseSerializer;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author yuguojia
 * @create 2018-02-24
 **/
@Setter
@Getter
public class TransactionIndex extends BaseSerializer {
    /**
     * the hash of the block witch the transaction belongs to
     */
    private String blockHash;

    /**
     * the hash of the transaction
     */
    private String txHash;

    /**
     * the index of this transaction in the block
     */
    private short txIndex;

    /**
     * outs spending info in this transaction. key: the out index, value: the spending transaction hash
     * if one out have not been spent, there is no key in the map.
     */
    private Map<Short, String> outsSpend;

    public boolean spent(short outIndex) {

        if (outsSpend != null && outsSpend.containsKey(outIndex)) {
            return true;
        }
        return false;
    }
}