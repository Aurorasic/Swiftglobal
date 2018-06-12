package com.higgsblock.global.browser.dao.entity;

import lombok.Data;

import java.sql.Timestamp;

/**
 * Block header information in detail
 *
 * @author yangshenghong
 * @date 2018-05-21
 */
@Data
public class BlockHeaderPO {
    /**
     * On the primary key
     */
    private long id;
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
    private Timestamp blockTime;
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
     * number of transaction
     */
    private int txNum;
    /**
     * Query the size of the block where the reward is located.
     */
    private int blockSize;
}
