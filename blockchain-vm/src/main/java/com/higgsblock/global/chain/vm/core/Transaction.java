package com.higgsblock.global.chain.vm.core;

/**
 * @author Chen Jiawei
 * @date 2018-09-13
 */
public class Transaction {
    private byte[] hash;
    private boolean isContractCreation;
    private byte[] contractAddress;
    private byte[] senderAddress;
    private byte[] gasPrice;
    private byte[] gasLimit;
    private byte[] value;
    private byte[] data;

    public Transaction() {}

    public Transaction(boolean isContractCreation, byte[] contractAddress, byte[] senderAddress, byte[] gasPrice, byte[] gasLimit, byte[] value, byte[] data) {
        this.isContractCreation = isContractCreation;
        this.contractAddress = contractAddress;
        this.senderAddress = senderAddress;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.value = value;
        this.data = data;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public boolean isContractCreation() {
        return isContractCreation;
    }

    public byte[] getContractAddress() {
        return contractAddress;
    }

    public byte[] getReceiveAddress() {
        return contractAddress;
    }

    public byte[] getSender() {
        return senderAddress;
    }

    public byte[] getGasPrice() {
        return gasPrice;
    }

    public byte[] getGasLimit() {
        return gasLimit;
    }

    public byte[] getValue() {
        return value;
    }

    public byte[] getData() {
        return data;
    }
}
