package com.higgsblock.global.chain.vm.api;

import java.math.BigInteger;

/**
 * @author Chen Jiawei
 * @date 2018-09-19
 */
public class TransferInfo {
    byte[] from;
    byte[] to;
    BigInteger value;

    public TransferInfo(byte[] from, byte[] to, BigInteger value) {
        this.from = from;
        this.to = to;
        this.value = value;
    }

    public byte[] getFrom() {
        return from;
    }

    public void setFrom(byte[] from) {
        this.from = from;
    }

    public byte[] getTo() {
        return to;
    }

    public void setTo(byte[] to) {
        this.to = to;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }
}
