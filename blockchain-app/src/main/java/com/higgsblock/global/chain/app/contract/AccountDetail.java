package com.higgsblock.global.chain.app.contract;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author tangkun
 * @date 2018-09-07
 */

public class AccountDetail implements Serializable{

    private String from;

    private String to;

    private BigInteger value;

    private BigInteger balance;

    private String currency;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }


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

    public AccountDetail(String from, String to, BigInteger value, BigInteger balance, String currency) {
        this.from = from;
        this.to = to;
        this.value = value;
        this.balance = balance;
        this.currency = currency;
    }
}
