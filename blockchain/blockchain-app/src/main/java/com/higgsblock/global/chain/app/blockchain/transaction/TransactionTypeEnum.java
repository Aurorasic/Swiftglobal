package com.higgsblock.global.chain.app.blockchain.transaction;

import lombok.Getter;
import lombok.Setter;

/**
 * Transaction type enum
 *
 * @author yuguojia
 * @create 2018-02-23
 **/
public enum TransactionTypeEnum {
    /**
     * transfer transaction without extra info
     */
    TRANSFER((short) 0),

    /**
     * transfer transaction with extra info
     */
    TRANSFER_EXTRA((short) 1),

    /**
     * transaction with only extra info
     */
    EXTRA((short) 2),

    /**
     * coinbase transaction for miner
     */
    COINBASE_MINE((short) 3),

    /**
     * coinbase transaction for signer
     */
    COINBASE_SIGNER((short) 4),

    /**
     * transaction created by system for miner joining in CAS Global system
     */
    JOIN_MINE((short) 5),

    /**
     * transaction updated by miner for updating normal miner address
     */
    UPDATE_MINE((short) 6),

    /**
     * transaction updated by system for updating system miner address
     */
    UPDATE_SYS_MINE((short) 7),

    /**
     * transaction updated by miner for stopping mining
     */
    STOP_MINE((short) 8),

    /**
     * transaction updated by miner for recovering mining
     */
    RECOVER_MINE((short) 9),

    /**
     * prepare mine coinbase
     */
    COINBASE_PREPARED_MINE((short) 10),
    /**
     * transaction mining machine management operation
     */
    OPERATED_MINE((short) 11);

    /**
     * the enum type of transaction
     */
    @Setter
    @Getter
    private short type;

    TransactionTypeEnum(short type) {
        this.type = type;
    }

    public static boolean containType(short type) {
        TransactionTypeEnum[] values = TransactionTypeEnum.values();
        for (TransactionTypeEnum typeEnum : values) {
            if (type == typeEnum.getType()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSystem(short type) {
        if (type == JOIN_MINE.getType() ||
                type == UPDATE_SYS_MINE.getType()) {
            return true;
        }
        return false;
    }
}