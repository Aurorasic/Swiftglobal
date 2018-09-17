package com.higgsblock.global.chain.vm.api;

/**
 * @author Chen Jiawei
 * @date 2018-09-14
 */
public class ContractExecutionException extends RuntimeException {
    public ContractExecutionException() {
        super();
    }

    public ContractExecutionException(String message) {
        super(message);
    }

    public ContractExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContractExecutionException(Throwable cause) {
        super(cause);
    }
}
