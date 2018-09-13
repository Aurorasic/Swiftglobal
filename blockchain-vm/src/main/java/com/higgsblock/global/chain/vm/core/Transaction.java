package com.higgsblock.global.chain.vm.core;

/**
 * @author Chen Jiawei
 * @date 2018-09-13
 */
public class Transaction {
    private boolean isCreate;
    private byte[] receiveAddress;
    private byte[] sendAddress;
    private byte[] gasPrice;
    private byte[] gasLimit;
    private byte[] value;
    private byte[] data;

    public Transaction() {}

    public Transaction(boolean isCreate, byte[] receiveAddress, byte[] sendAddress, byte[] gasPrice, byte[] gasLimit, byte[] value, byte[] data) {
        this.isCreate = isCreate;
        this.receiveAddress = receiveAddress;
        this.sendAddress = sendAddress;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.value = value;
        this.data = data;
    }

    public boolean isContractCreation() {
        return isCreate;
    }

    public byte[] getContractAddress() {
        return receiveAddress;
    }

    public byte[] getReceiveAddress() {
        return receiveAddress;
    }

    public byte[] getSender() {
        return sendAddress;
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
