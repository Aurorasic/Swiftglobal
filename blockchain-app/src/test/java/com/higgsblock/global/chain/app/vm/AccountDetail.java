package com.higgsblock.global.chain.app.vm;

import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author tangkun
 * @date 2018-09-07
 */
@AllArgsConstructor
public class AccountDetail implements Serializable{

    private String from;

    private String to;

    private BigInteger value;

    private BigInteger balance;

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


}
