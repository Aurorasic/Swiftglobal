package com.higgsblock.global.browser.dao.entity;

import lombok.Data;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
@Data
public class TransactionPO {
    /**
     * On the primary key
     */
    private long id;
    /**
     * The exchange is at the height of the block.
     */
    private long height;
    /**
     * The exchange is hashing in the block.
     */
    private String blockHash;
    /**
     * TransactionPO hash
     */
    private String transactionHash;
    /**
     * version
     */
    private int version;
    /**
     * Time in Force
     */
    private long lockTime;
    /**
     * extraneous information
     */
    private String extra;
}
