package cn.primeledger.cas.global.common.event;

import cn.primeledger.cas.global.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

/**
 * @author yuguojia
 * @date 2018/04/04
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockPersistedEvent extends BaseSerializer {
    /**
     * the height of the persisted block
     */
    private long height;

    /**
     * the block-hash of the persisted block
     */
    private String blockHash;

    /**
     * the best block hash on this height.
     * if this value is empty, there has no best block on the height.
     * if this value equals blockHash, then the block is best block, otherwise not best block
     */
    private String bestBlockHash;

    public boolean hasBestBlock() {
        if (!StringUtils.isEmpty(bestBlockHash)) {
            return true;
        }
        return false;
    }

    public boolean isBestBlock() {
        if (hasBestBlock() &&
                StringUtils.equals(blockHash, bestBlockHash)) {
            return true;
        }
        return false;
    }
}