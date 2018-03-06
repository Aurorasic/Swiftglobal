package cn.primeledger.cas.global.blockchain;

import cn.primeledger.cas.global.entity.BaseSerializer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
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
     * block height begin with 1. It's as the key of db table.
     */
    private long height;

    /**
     * all block hash with the same height
     */
    private ArrayList<String> blockHashs;

    /**
     * the block index begin with 0 in blockHashs on the best chain with the same height.
     * if bestIndex < 0, there is no block on the best chain with this height
     */
    private int bestIndex;

    public BlockIndex(long height, ArrayList<String> blockHashs, int bestIndex) {
        this.height = height;
        this.blockHashs = blockHashs;
        this.bestIndex = bestIndex;
    }

    public boolean valid() {
        if (height < 1) {
            return false;
        }
        if (CollectionUtils.isEmpty(blockHashs)) {
            return false;
        }
        if (bestIndex > blockHashs.size() - 1) {
            return false;
        }
        return true;
    }

    public void addBlockHash(String blockHash, boolean toBest) {
        if (CollectionUtils.isEmpty(blockHashs)) {
            blockHashs = new ArrayList<>(1);
            bestIndex = -1;
        }
        blockHashs.add(blockHash);
        if (toBest) {
            bestIndex = blockHashs.size() - 1;
        }
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

    public String getBestBlockHash() {
        if (bestIndex > -1 && bestIndex < blockHashs.size()) {
            return blockHashs.get(bestIndex);
        }
        return null;
    }

    public boolean hasBestBlock() {
        if (bestIndex > -1 && bestIndex < blockHashs.size()) {
            return true;
        }
        return false;
    }

    public int getIndex(String blockHash) {
        if (StringUtils.isEmpty(blockHash) || CollectionUtils.isEmpty(blockHashs)) {
            return -1;
        }

        for (int i = 0; i < blockHashs.size(); i++) {
            if (StringUtils.equals(blockHashs.get(i), blockHash)) {
                return i;
            }
        }
        return -1;
    }

    public boolean switchToBestChain(String blockHash) {
        int index = getIndex(blockHash);
        if (index < 0) {
            return false;
        }
        //may be the same
        bestIndex = index;
        return true;
    }
}