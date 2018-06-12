package com.higgsblock.global.browser.service.bo;

import lombok.Data;

/**
 * @author Su Jiulong
 * @date 2018-05-23
 */
@Data
public class TransactionOutPutBO {
    /**
     * transfer amount
     */
    private String amount;
    /**
     * transfer currency
     */
    private String currency;
    /**
     * output to script type
     */
    private LockScriptBO lockScript;
}
