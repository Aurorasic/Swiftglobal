package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.entity.BaseSerializer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

/**
 * @author yuguojia
 * @create 2018-02-24
 **/
@Setter
@Getter
public class TransactionOutPoint extends BaseSerializer {
    /**
     * the hash of source transaction for spending
     */
    private String hash;

    /**
     * the index out of ource transaction
     */
    private int index;

    public boolean valid() {
        if (StringUtils.isEmpty(hash) || index < 0) {
            return false;
        }
        return true;
    }
}