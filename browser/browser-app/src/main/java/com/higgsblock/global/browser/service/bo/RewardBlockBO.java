package com.higgsblock.global.browser.service.bo;

import lombok.Data;

/**
 * @author Su Jiulong
 * @date 2018-05-24
 */
@Data
public class RewardBlockBO {
    /**
     * Block height
     */
    private long height;
    /**
     * Block generation time
     */
    private String  createAt;
    /**
     * block size
     */
    private int size;
    /**
     * Block hash
     */
    private String blockHash;
    /**
     * the amount of producing block's reward
     */
    private String earnings;
    /**
     * currency
     */
    private String currency;
}