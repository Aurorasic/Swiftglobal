package com.higgsblock.global.chain.vm.core;

/**
 * @author Chen Jiawei
 * @date 2018-09-13
 */
public class Block {
    private byte[] parentHash;
    private byte[] coinbase;
    private long timestamp;
    private long number;
    private byte[] difficulty;
    private byte[] gasLimit;

    public Block(byte[] parentHash, byte[] coinbase, long timestamp, long number, byte[] difficulty, byte[] gasLimit) {
        this.parentHash = parentHash;
        this.coinbase = coinbase;
        this.timestamp = timestamp;
        this.number = number;
        this.difficulty = difficulty;
        this.gasLimit = gasLimit;
    }

    public byte[] getParentHash() {
        return parentHash;
    }

    public byte[] getCoinbase() {
        return coinbase;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getNumber() {
        return number;
    }

    public byte[] getDifficulty() {
        return difficulty;
    }

    public byte[] getGasLimit() {
        return gasLimit;
    }
}
