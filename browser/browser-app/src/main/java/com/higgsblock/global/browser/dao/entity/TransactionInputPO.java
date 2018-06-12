package com.higgsblock.global.browser.dao.entity;

import lombok.Data;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
@Data
public class TransactionInputPO {
    /**
     * On the primary key
     */
    private long id;
    /**
     * Current transaction hash
     */
    private String transactionHash;
    /**
     * The current exchange index on the transaction list.
     */
    private int index;
    /**
     * The previous transaction hash.
     */
    private String preTransactionHash;
    /**
     * Previous transaction output index.
     */
    private short preOutIndex;
    /**
     * Public key list
     */
    private String addressList;
}
