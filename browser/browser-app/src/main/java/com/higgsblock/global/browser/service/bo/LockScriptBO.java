package com.higgsblock.global.browser.service.bo;

import lombok.Data;

/**
 * @author Su Jiulong
 * @date 2018-05-25
 */
@Data
public class LockScriptBO {
    /**
     * TransactionPO type
     * 0 for P2PKH
     * 1 for P2SH
     * 2 for P2PK.
     */
    private int tpye;
    /**
     * address
     */
    private String address;
}
