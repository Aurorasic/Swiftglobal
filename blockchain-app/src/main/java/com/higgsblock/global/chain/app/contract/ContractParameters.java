package com.higgsblock.global.chain.app.contract;

import java.math.BigInteger;

/**
 * Parameters container for contract creation or contract call.
 *
 * @author Chen Jiawei
 * @date 2018-09-27
 */
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

    public ContractParameters(short vmVersion, BigInteger gasPrice, long gasLimit, byte[] bytecode) {
        this.vmVersion = vmVersion;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.bytecode = bytecode;
    }

    public short getVmVersion() {
        return vmVersion;
    }

    public void setVmVersion(short vmVersion) {
        this.vmVersion = vmVersion;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public byte[] getBytecode() {
        return bytecode;
    }

    public void setBytecode(byte[] bytecode) {
        this.bytecode = bytecode;
    }
}
