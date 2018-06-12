package com.higgsblock.global.browser.service.bo;

import lombok.Data;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-23
 */
@Data
public class TransactionBO {
    /**
     * The transaction's hash
     */
    private String txHash;
    /**
     * version
     */
    private int version;
    /**
     * TransactionInputBO list
     */
    private List<TransactionInputBO> inputs;
    /**
     * TransactionOutPutBO list
     */
    private List<TransactionOutPutBO> outputs;
    /**
     * lock after pointed block height of time
     */
    private long lockTime;
    /**
     * extra info for this transaction
     */
    private String extra;

    private PubKeyAndSignPairBO pubKeyAndSignPair;

}
