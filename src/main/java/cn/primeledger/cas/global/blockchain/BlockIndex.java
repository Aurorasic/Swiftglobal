package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.entity.BaseSerializer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

/**
 * @author yuguojia
 * @create 2018-02-24
 **/
@Setter
@Getter
public class BlockIndex extends BaseSerializer {

    /**
     * block height begin with 1
     */
    private long height;

    /**
     * all block hash with the same height
     */
    private ArrayList<String> blockHashs;

    /**
     * the block index in blockHashs on the best chain with the same height.
     * if bestIndex < 0, there is no block on the best chain with this height
     */
    private int bestIndex;

    public BlockIndex(long height, ArrayList<String> blockHashs, int bestIndex) {
        this.height = height;
        this.blockHashs = blockHashs;
        this.bestIndex = bestIndex;
    }

    public boolean isBest(String blockHash) {
        if (StringUtils.isEmpty(blockHash)) {
            return false;
        }

        if (blockHashs != null && bestIndex >= 0 && bestIndex < blockHashs.size()) {
            String bestBlockHash = blockHashs.get(bestIndex);
            if (StringUtils.equals(blockHash, bestBlockHash)) {
                return true;
            }
        }
        return false;
    }
}