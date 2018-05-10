package com.higgsblock.global.chain.app.blockchain.transaction;

/**
 * @author chenjiawei
 * @date 2018-03-28
 */
public class TransactionException extends Exception {
    private static final long serialVersionUID = 6181790433029797463L;

    public TransactionException() {
        super();
    }

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(Throwable cause) {
        super(cause);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
