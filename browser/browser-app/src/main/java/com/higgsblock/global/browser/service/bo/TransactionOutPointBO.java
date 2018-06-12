package com.higgsblock.global.browser.service.bo;

import lombok.Data;

/**
 * @author Su Jiulong
 * @date 2018-05-23
 */
@Data
public class TransactionOutPointBO {
    /**
     * the hash of source transaction for spending
     */
    private String hash;

    /**
     * the index out of source transaction
     */
    private short index;
}
