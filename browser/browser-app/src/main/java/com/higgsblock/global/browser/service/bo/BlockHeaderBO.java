package com.higgsblock.global.browser.service.bo;

import lombok.Data;

/**
 * @author yangshenghong
 * @date 2018-05-23
 */
@Data
public class BlockHeaderBO {
    /**
     * Block height
     */
    private long height;
    /**
     * Block hash
     */
    private String blockHash;
    /**
     * Block generation time
     */
    private String blockTime;
    /**
     * Previous block hash
     */
    private String preBlockHash;
    /**
     * MinerPO address
     */
    private String minerAddress;
    /**
     * Witness address
     */
    private String witnessAddress;
    /**
     * Query the size of the block where the reward is located.
     */
    private int blockSize;

}
