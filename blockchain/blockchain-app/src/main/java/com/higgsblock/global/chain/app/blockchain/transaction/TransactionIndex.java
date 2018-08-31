package com.higgsblock.global.chain.app.blockchain.transaction;

import com.alibaba.fastjson.annotation.JSONType;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @author yuguojia
 * @create 2018-02-24
 **/
@Setter
@Getter
@NoArgsConstructor
@JSONType(includes = {"blockHash", "txHash", "txIndex", "outsSpend"})
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
     * if one out have not been isSpent, there is no key in the map.
     */
    private Map<Short, String> outsSpend;

    public boolean valid() {
        if (StringUtils.isEmpty(blockHash)) {
            return false;
        }
        if (StringUtils.isEmpty(txHash)) {
            return false;
        }
        if (txIndex < 0) {
            return false;
        }
        if (outsSpend.isEmpty()) {
            return false;
        }
        return true;
    }

    public TransactionIndex(String blockHash, String txHash, short txIndex) {
        this.blockHash = blockHash;
        this.txHash = txHash;
        this.txIndex = txIndex;
    }
}