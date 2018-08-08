package com.higgsblock.global.chain.app.api.vo;

import com.higgsblock.global.chain.app.blockchain.Block;
import com.higgsblock.global.chain.common.entity.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author huangshengli
 * @since 2018-07-16
 */
@Data
@AllArgsConstructor
public class SimpleBlockVO extends BaseSerializer {
    /**
     * block hash
     */
    private String blockHash;
    /**
     * address of the miner who produced this block
     */
    private String minerAddress;
    /**
     * the height of block
     */
    private Long height;

    public SimpleBlockVO() {
        super();
    }

    public SimpleBlockVO(Block block) {
        this.height = block.getHeight();
        this.blockHash = block.getHash();
        this.minerAddress = block.getMinerSigPair().getAddress();
    }
}