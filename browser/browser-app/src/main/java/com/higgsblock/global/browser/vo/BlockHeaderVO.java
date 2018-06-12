package com.higgsblock.global.browser.vo;

import lombok.Data;

/**
 * @author yangshenghong
 * @date 2018-05-25
 */
@Data
public class BlockHeaderVO {
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
}
