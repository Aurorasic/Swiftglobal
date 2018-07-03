package com.higgsblock.global.browser.dao.entity;

import lombok.Data;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
@Data
public class RewardPO {
    /**
     * On the primary key
     */
    private long id;
    /**
     * The height of the block he dug or witnessed.
     */
    private long height;
    /**
     * miner address
     */
    private String address;
    /**
     * Query the reward for the block hash.
     */
    private String blockHash;
    /**
     * The total reward
     */
    private String amount;
    /**
     * transaction fee
     */
    private String fee;
    /**
     * RewardPO currency (default CAS)
     */
    private String currency;
    /**
     * Types of rewards
     * 0: miner reward
     * 1: witness reward
     */
    private int type;
}
