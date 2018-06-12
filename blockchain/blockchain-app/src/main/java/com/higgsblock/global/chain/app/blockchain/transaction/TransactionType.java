package com.higgsblock.global.chain.app.blockchain.transaction;

import lombok.Getter;
import lombok.Setter;

/**
 * @author kongyu
 * @date 2018-03-26 15:41
 */
public enum TransactionType {
    /**
     * miner rewards
     */
    COINBASE("COINBASE", "COINBASE"),
    /**
     * witness rewards
     */
    WITNESS("WITNESS", "WITNESS"),
    /**
     * common transactions
     */
    NORMAL("NORMAL", "NORMAL");

    @Getter
    private String code;

    @Getter
    private String desc;

    TransactionType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
