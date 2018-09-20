package com.higgsblock.global.chain.vm.core;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author tangkun
 * @date 2018-09-07
 */

public class AccountDetail implements Serializable{

    private byte[] from;

    private byte[] to;

    private BigInteger value;

    private BigInteger balance;

    private String currency;

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public AccountDetail(byte[] from, byte[] to, BigInteger value, BigInteger balance, String currency) {
        this.from = from;
        this.to = to;
        this.value = value;
        this.balance = balance;
        this.currency = currency;
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
}
