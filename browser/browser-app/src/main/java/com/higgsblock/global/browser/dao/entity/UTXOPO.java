package com.higgsblock.global.browser.dao.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yangshenghong
 * @date 2018-05-21
 */
@Data
public class UTXOPO {
    /**
     * On the primary key
     */
    private long id;
    /**
     * transaction hash
     */
    private String transactionHash;
    /**
     * The index of the transaction output starts at 0.
     */
    private int outIndex;
    /**
     * money
     */
    private String amount;
    /**
     * currency
     */
    private String currency;
    /**
     * TransactionPO type
     * 0 for P2PKH
     * 1 for P2SH
     * 2 for P2PK.
     */
    private int scriptType;
    /**
     * address
     */
    private String address;
}
