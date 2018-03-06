package cn.primeledger.cas.global.blockchain.transaction;

import cn.primeledger.cas.global.entity.BaseSerializer;
import lombok.Data;

/**
 * @author baizhengwen
 * @create 2018-03-06
 **/
@Data
public class BaseInput extends BaseSerializer {
    /**
     * the sources of current spending
     */
    private TxOutPoint prevOut;

    public boolean valid() {
        if (null == prevOut) {
            return false;
        }
        return prevOut.valid();
    }
}