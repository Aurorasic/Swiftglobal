package com.higgsblock.global.browser.vo;

import com.higgsblock.global.browser.service.bo.PubKeyAndSignPairBO;
import com.higgsblock.global.browser.service.bo.TransactionInputBO;
import com.higgsblock.global.browser.service.bo.TransactionOutPutBO;
import lombok.Data;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-23
 */
@Data
public class TransactionVO {
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
