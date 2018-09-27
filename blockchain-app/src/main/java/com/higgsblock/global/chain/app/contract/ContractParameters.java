package com.higgsblock.global.chain.app.contract;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * Parameters container for contract creation or contract call.
 *
 * @author Chen Jiawei
 * @date 2018-09-27
 */
@Data
@AllArgsConstructor
public class ContractParameters {
    /**
     * Version of virtual machine.
     */
    private short vmVersion;
    /**
     * Gas price of a unit transaction creator is willing to pay.
     */
    private BigInteger gasPrice;
    /**
     * Maximum of gas amount for transaction being accepted.
     */
    private long gasLimit;
    /**
     * Byte code of contract creation or contract call.
     */
    private byte[] bytecode;

    public ContractParameters(BigInteger gasPrice, long gasLimit, byte[] bytecode) {
        this((short) 0, gasPrice, gasLimit, bytecode);
    }
}
