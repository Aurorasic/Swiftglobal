package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.entity.BaseSerializer;
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
public class TxOutPoint extends BaseSerializer {
    /**
     * the hash of source transaction for spending
     */
    private String hash;

    /**
     * the index out of ource transaction
     */
    private short index;

    public boolean valid() {
        if (StringUtils.isEmpty(hash) || index < 0) {
            return false;
        }
        return true;
    }
}