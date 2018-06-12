package com.higgsblock.global.browser.dao.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
@Data
public class TransactionOutputPO {
    /**
     * On the primary key
     */
    private long id;
    /**
     * transaction hash
     */
    private String transactionHash;
    /**
     * The output index
     */
    private int index;
    /**
     * money
     */
    private String amount;
    /**
     * currency type
     */
    private String currency;
    /**
     * Locking script type
     */
    private int scriptType;
    /**
     * address
     */
    private String address;
}
