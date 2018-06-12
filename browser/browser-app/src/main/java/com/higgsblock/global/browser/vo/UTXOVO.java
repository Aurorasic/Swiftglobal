package com.higgsblock.global.browser.vo;

import lombok.Data;

/**
 * @author Su Jiulong
 * @date 2018-05-29
 */
@Data
public class UTXOVO {
    /**
     * transaction hash
     */
    private String transactionHash;
    /**
     * The index of the transaction output starts at 0.
     */
    private short outIndex;
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
