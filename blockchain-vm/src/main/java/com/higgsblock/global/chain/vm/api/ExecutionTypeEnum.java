package com.higgsblock.global.chain.vm.api;

/**
 * Type of contract execution.
 *
 * @author Chen Jiawei
 * @date 2018-09-17
 */
public enum ExecutionTypeEnum {
    /**
     * Create contract.
     */
    CONTRACT_CREATION,

    /**
     * Call contract.
     */
    CONTRACT_CALL,

    /**
     * Call precompiled contract.
     */
    PRECOMPILED_CONTRACT_CALL
}
