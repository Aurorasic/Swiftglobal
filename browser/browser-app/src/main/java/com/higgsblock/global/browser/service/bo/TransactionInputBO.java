package com.higgsblock.global.browser.service.bo;

import lombok.Data;

import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018-05-23
 */
@Data
public class TransactionInputBO {
    /**
     *  A transaction output that points to a particular transaction.
     */
    private TransactionOutPointBO preOut;
    /**
     * Multiple signature list
     */
    private List<String> unLockScript;
}
