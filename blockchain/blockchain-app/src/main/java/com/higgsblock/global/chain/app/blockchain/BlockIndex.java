package com.higgsblock.global.chain.app.blockchain;

import com.alibaba.fastjson.annotation.JSONType;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@JSONType(includes = {"height", "blockHashs", "bestBlockHash"})
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
     * if null ,this block has not been confirmed on main chain
     */
    private String bestBlockHash;

    public BlockIndex(long height, ArrayList<String> blockHashs, String bestBlockHash) {
        this.height = height;
        this.blockHashs = blockHashs;
        this.bestBlockHash = bestBlockHash;
    }

    public boolean valid() {
        if (height < 1) {
            return false;
        }
        if (CollectionUtils.isEmpty(blockHashs)) {
            return false;
        }
        return true;
    }

    public void addBlockHash(String blockHash, boolean toBest) {
        if (CollectionUtils.isEmpty(blockHashs)) {
            blockHashs = new ArrayList<>(1);
        }
        blockHashs.add(blockHash);
        if (toBest) {
            bestBlockHash = blockHash;
        }
    }

    public boolean containsBlockHash(String blockHash) {
        if (CollectionUtils.isNotEmpty(blockHashs)) {
            return blockHashs.contains(blockHash);
        }
        return false;
    }

    public boolean isBest(String blockHash) {
        if (StringUtils.isEmpty(blockHash) || StringUtils.isEmpty(bestBlockHash)) {
            return false;
        }

        return StringUtils.equals(blockHash, bestBlockHash);
    }

    public String getFirstBlockHash() {
        if (CollectionUtils.isNotEmpty(blockHashs)) {
            return blockHashs.get(0);
        }
        return null;
    }

    public boolean hasBestBlock() {
        return StringUtils.isNotEmpty(bestBlockHash);
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
}