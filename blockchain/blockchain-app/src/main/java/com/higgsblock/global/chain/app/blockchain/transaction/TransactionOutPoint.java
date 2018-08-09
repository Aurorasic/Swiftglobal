package com.higgsblock.global.chain.app.blockchain.transaction;

import com.alibaba.fastjson.annotation.JSONType;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
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
@JSONType(includes = {"hash", "index"})
public class TransactionOutPoint extends BaseSerializer {
    /**
     * the hash of source transaction for spending
     */
    private String hash;

    /**
     * the index out of source transaction
     */
    private short index;

    public boolean valid() {
        if (StringUtils.isEmpty(hash)) {
            return false;
        }
        if (index < 0) {
            return false;
        }
        return true;
    }

    public String getKey() {
        return hash + "_" + index;
    }

}